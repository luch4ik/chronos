
import React, { useState, useEffect } from 'react';
import { AnimatePresence } from 'framer-motion';
import { v4 as uuidv4 } from 'uuid';
import { Settings } from 'lucide-react'; // Import Settings icon
import { Background } from './components/Background';
import { ClockDisplay } from './components/ClockDisplay';
import { AlarmManager } from './components/AlarmManager';
import { AlarmScreen } from './components/AlarmScreen';
import { SettingsModal } from './components/SettingsModal';
import { WakeUpCheckModal } from './components/WakeUpCheckModal';
import { EmergencyModal } from './components/EmergencyModal';
import { useTime } from './hooks/useTime';
import { Alarm, ChallengeConfig, AppSettings, WakeUpCheckConfig, EmergencyContactConfig, AlarmAudioConfig } from './types';
import { audioService } from './services/audioService';
import { triggerHaptic } from './utils';

interface PendingCheck {
  alarmId: string;
  triggerTime: number; // timestamp
  confirmWindow: number; // minutes
  originalAlarm: Alarm;
}

const App: React.FC = () => {
  const now = useTime();
  
  // Initialize alarms with migration
  const [alarms, setAlarms] = useState<Alarm[]>(() => {
    const saved = localStorage.getItem('chronos_alarms');
    if (!saved) return [];
    try {
        const parsed = JSON.parse(saved);
        return parsed.map((a: any) => {
            // Migration 1: Challenge string to array
            if (a.challenge && !a.challenges) {
                 const newChallenges: ChallengeConfig[] = [];
                 if (a.challenge !== 'NONE') {
                    newChallenges.push({
                        id: uuidv4(),
                        type: a.challenge,
                        params: a.challenge === 'BURST' ? { count: 15 } : { difficulty: 'NORMAL' }
                    });
                 }
                 a.challenges = newChallenges;
                 delete a.challenge;
            }
            // Migration 2: Custom audio URL to Audio Config
            if (a.customAudioUrl && !a.audio) {
                a.audio = {
                    source: 'URL',
                    url: a.customAudioUrl
                };
                delete a.customAudioUrl;
            }
            return a;
        });
    } catch (e) {
        return [];
    }
  });

  // Settings State with Theme
  const [settings, setSettings] = useState<AppSettings>(() => {
      const defaultSettings: AppSettings = {
          timeFormat: '24h',
          uninstallProtection: false,
          volumeOverride: false,
          rebootProtection: false,
          theme: 'system'
      };
      
      const saved = localStorage.getItem('chronos_settings');
      if (saved) {
          try { 
              const parsed = JSON.parse(saved);
              return { ...defaultSettings, ...parsed };
          } catch (e) {}
      }
      return defaultSettings;
  });
  
  const [showSettings, setShowSettings] = useState(false);
  const [triggeredAlarm, setTriggeredAlarm] = useState<Alarm | null>(null);
  const [previewAlarm, setPreviewAlarm] = useState<Alarm | null>(null);
  
  // Wake Up Check States
  const [pendingChecks, setPendingChecks] = useState<PendingCheck[]>([]);
  const [activeWakeUpCheck, setActiveWakeUpCheck] = useState<{ alarm: Alarm, deadline: number } | null>(null);

  // Emergency Protocol State
  const [alarmStartTime, setAlarmStartTime] = useState<number | null>(null);
  const [emergencyTriggered, setEmergencyTriggered] = useState(false);

  // ANDROID: Screen Wake Lock
  useEffect(() => {
    let wakeLock: any = null;
    const requestWakeLock = async () => {
      try {
        // @ts-ignore
        if ('wakeLock' in navigator) {
          // @ts-ignore
          wakeLock = await navigator.wakeLock.request('screen');
        }
      } catch (err) {
        console.log('Wake Lock request failed');
      }
    };
    
    // Initial request
    requestWakeLock();
    
    // Re-request on visibility change (e.g. user tabs back)
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        requestWakeLock();
      }
    };
    
    // ANDROID: Warmup audio on first interaction
    const handleInteraction = () => {
        audioService.warmup();
        window.removeEventListener('click', handleInteraction);
        window.removeEventListener('touchstart', handleInteraction);
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    window.addEventListener('click', handleInteraction);
    window.addEventListener('touchstart', handleInteraction);

    return () => {
        document.removeEventListener('visibilitychange', handleVisibilityChange);
        window.removeEventListener('click', handleInteraction);
        window.removeEventListener('touchstart', handleInteraction);
        if(wakeLock) wakeLock.release();
    }
  }, []);

  useEffect(() => {
    localStorage.setItem('chronos_alarms', JSON.stringify(alarms));
  }, [alarms]);

  // Theme Logic
  useEffect(() => {
    localStorage.setItem('chronos_settings', JSON.stringify(settings));
    
    const applyTheme = (isDark: boolean) => {
        const metaThemeColor = document.querySelector("meta[name=theme-color]");
        if (isDark) {
            document.documentElement.classList.add('dark');
            if (metaThemeColor) metaThemeColor.setAttribute("content", "#1A1918");
        } else {
            document.documentElement.classList.remove('dark');
            if (metaThemeColor) metaThemeColor.setAttribute("content", "#FAF8F6");
        }
    };

    if (settings.theme === 'system') {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
        applyTheme(mediaQuery.matches);

        const handler = (e: MediaQueryListEvent) => applyTheme(e.matches);
        mediaQuery.addEventListener('change', handler);
        return () => mediaQuery.removeEventListener('change', handler);
    } else {
        applyTheme(settings.theme === 'dark');
    }
  }, [settings.theme]);

  // Main Timer Loop
  useEffect(() => {
    const nowMs = now.getTime();
    const currentHours = now.getHours().toString().padStart(2, '0');
    const currentMinutes = now.getMinutes().toString().padStart(2, '0');
    const currentSeconds = now.getSeconds();
    const currentTimeStr = `${currentHours}:${currentMinutes}`;
    const currentDay = now.getDay();

    // 1. Check for Alarms
    if (currentSeconds === 0) {
      const found = alarms.find(a => {
        if (!a.isActive || a.time !== currentTimeStr) return false;
        if (a.days.length === 0) return true;
        return a.days.includes(currentDay);
      });

      if (found && !triggeredAlarm && !previewAlarm) {
        setPendingChecks(prev => prev.filter(p => p.alarmId !== found.id));
        setActiveWakeUpCheck(null); 
        
        setTriggeredAlarm(found);
        setAlarmStartTime(Date.now());
        setEmergencyTriggered(false);
        audioService.playAlarm(found.audio);
      }
    }

    // 2. Monitor Emergency Protocol
    if (triggeredAlarm && triggeredAlarm.emergencyContact?.enabled && alarmStartTime && !emergencyTriggered) {
        const elapsedMinutes = (nowMs - alarmStartTime) / 60000;
        if (elapsedMinutes >= triggeredAlarm.emergencyContact.triggerDelay) {
            setEmergencyTriggered(true);
        }
    }

    // 3. Monitor Pending Wake Up Checks
    const checkToTrigger = pendingChecks.find(p => nowMs >= p.triggerTime);
    
    if (checkToTrigger && !triggeredAlarm && !previewAlarm && !activeWakeUpCheck) {
        setPendingChecks(prev => prev.filter(p => p !== checkToTrigger));
        setActiveWakeUpCheck({
            alarm: checkToTrigger.originalAlarm,
            deadline: nowMs + (checkToTrigger.confirmWindow * 60 * 1000)
        });
        audioService.playAlarm(checkToTrigger.originalAlarm.audio); 
    }

    // 4. Monitor Active Wake Up Check Deadline
    if (activeWakeUpCheck) {
        if (nowMs >= activeWakeUpCheck.deadline) {
            const failedAlarm = activeWakeUpCheck.alarm;
            setActiveWakeUpCheck(null);
            setTriggeredAlarm(failedAlarm); 
            setAlarmStartTime(Date.now()); 
            setEmergencyTriggered(false);
            audioService.playAlarm(failedAlarm.audio);
        }
    }

  }, [now, alarms, triggeredAlarm, previewAlarm, pendingChecks, activeWakeUpCheck, alarmStartTime, emergencyTriggered]);

  const addAlarm = (time: string, days: number[], challenges: ChallengeConfig[], wakeUpCheck: WakeUpCheckConfig, emergencyContact: EmergencyContactConfig, audioConfig: AlarmAudioConfig) => {
    const newAlarm: Alarm = {
      id: uuidv4(),
      time,
      label: 'ALARM',
      isActive: true,
      days,
      challenges,
      wakeUpCheck,
      emergencyContact,
      audio: audioConfig
    };
    setAlarms(prev => [...prev, newAlarm].sort((a, b) => a.time.localeCompare(b.time)));
  };

  const updateAlarm = (updatedAlarm: Alarm) => {
    setAlarms(prev => prev.map(a => a.id === updatedAlarm.id ? updatedAlarm : a).sort((a, b) => a.time.localeCompare(b.time)));
  };

  const toggleAlarm = (id: string) => {
    setAlarms(prev => prev.map(a => 
      a.id === id ? { ...a, isActive: !a.isActive } : a
    ));
    setPendingChecks(prev => prev.filter(p => p.alarmId !== id));
  };

  const deleteAlarm = (id: string) => {
    setAlarms(prev => prev.filter(a => a.id !== id));
    setPendingChecks(prev => prev.filter(p => p.alarmId !== id));
  };

  const dismissAlarm = () => {
    audioService.stopAlarm();
    
    if (triggeredAlarm) {
      const alarmToProcess = triggeredAlarm;
      setTriggeredAlarm(null);
      setAlarmStartTime(null);
      setEmergencyTriggered(false);

      if (alarmToProcess.days.length === 0) {
        toggleAlarm(alarmToProcess.id);
      } else {
         if (alarmToProcess.wakeUpCheck?.enabled) {
             const delayMs = alarmToProcess.wakeUpCheck.checkDelay * 60 * 1000;
             setPendingChecks(prev => [
                 ...prev, 
                 {
                     alarmId: alarmToProcess.id,
                     triggerTime: Date.now() + delayMs,
                     confirmWindow: alarmToProcess.wakeUpCheck!.confirmWindow,
                     originalAlarm: alarmToProcess
                 }
             ]);
         }
      }
    }
    
    if (previewAlarm) {
      setPreviewAlarm(null);
    }
  };
  
  const handleConfirmWakeUp = () => {
      setActiveWakeUpCheck(null);
      audioService.stopAlarm(); 
  };

  const handlePreview = (challenges: ChallengeConfig[]) => {
    const tempAlarm: Alarm = {
        id: 'preview',
        time: 'PREVIEW',
        label: 'TEST MODE',
        isActive: true,
        days: [],
        challenges
    };
    setPreviewAlarm(tempAlarm);
  };

  return (
    <div className="relative h-screen w-full font-sans text-neoBlack overflow-y-auto transition-colors duration-300">
      <Background />
      
      <main className="relative z-10 flex flex-col items-center min-h-screen pt-4 md:pt-8">
        <header className="w-full max-w-2xl px-4 mb-2 flex justify-between items-center">
             <div className="text-textMain font-serif font-bold text-lg tracking-tight">
                Chronos
             </div>
             <div className="flex gap-2">
                <button 
                  onClick={() => { triggerHaptic('light'); setShowSettings(true); }}
                  className="bg-surface/50 border border-border p-2 rounded-full hover:bg-surfaceHover transition-all text-textMain"
                  title="Settings"
                >
                  <Settings size={20} />
                </button>
             </div>
        </header>

        <ClockDisplay date={now} settings={settings} />
        
        <AlarmManager 
          alarms={alarms}
          settings={settings}
          addAlarm={addAlarm}
          updateAlarm={updateAlarm}
          toggleAlarm={toggleAlarm}
          deleteAlarm={deleteAlarm}
          onPreview={handlePreview}
        />
      </main>

      <AnimatePresence>
        {showSettings && (
            <SettingsModal 
                settings={settings}
                onUpdate={setSettings}
                onClose={() => setShowSettings(false)}
            />
        )}
        
        {(triggeredAlarm || previewAlarm) && !emergencyTriggered && (
          <AlarmScreen 
            alarm={triggeredAlarm || previewAlarm!} 
            onDismiss={dismissAlarm}
            isPreview={!!previewAlarm}
          />
        )}

        {triggeredAlarm && emergencyTriggered && triggeredAlarm.emergencyContact && (
            <EmergencyModal 
                config={triggeredAlarm.emergencyContact} 
                onDismiss={dismissAlarm}
            />
        )}

        {activeWakeUpCheck && (
            <WakeUpCheckModal 
                confirmWindowMinutes={activeWakeUpCheck.alarm.wakeUpCheck?.confirmWindow || 1}
                deadline={activeWakeUpCheck.deadline}
                onConfirm={handleConfirmWakeUp}
            />
        )}
      </AnimatePresence>
    </div>
  );
};

export default App;
