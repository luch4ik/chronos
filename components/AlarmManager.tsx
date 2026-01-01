
import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { v4 as uuidv4 } from 'uuid';
import { Plus, X, Brain, Zap, Trash2, Play, Keyboard, Grid3X3, Bluetooth, Activity, Eye, Siren, Search, Smartphone, Loader2, Ghost, Music, Radio, Upload, FileAudio, Volume2, Clock } from 'lucide-react';
import { Alarm, ChallengeConfig, ChallengeType, AppSettings, WakeUpCheckConfig, EmergencyContactConfig, AlarmAudioConfig, GeneratedSoundType, SystemSoundType } from '../types';
import { AlarmItem } from './AlarmItem';
import { triggerHaptic, getTimeUntil } from '../utils';
import { audioService } from '../services/audioService';
import { TimePickerWheel } from './TimePickerWheel';

interface AlarmManagerProps {
  alarms: Alarm[];
  settings: AppSettings;
  addAlarm: (time: string, days: number[], challenges: ChallengeConfig[], wakeUpCheck: WakeUpCheckConfig, emergencyContact: EmergencyContactConfig, audioConfig: AlarmAudioConfig) => void;
  updateAlarm: (alarm: Alarm) => void;
  toggleAlarm: (id: string) => void;
  deleteAlarm: (id: string) => void;
  onPreview: (challenges: ChallengeConfig[]) => void;
}

const ConfigGroup: React.FC<{ label: string; children: React.ReactNode }> = ({ label, children }) => (
    <div className="flex flex-col gap-1 flex-1 min-w-[100px]">
        <span className="text-xs font-bold font-display text-textMain uppercase tracking-wide border-l-4 border-accent pl-1">{label}</span>
        {children}
    </div>
);

export const AlarmManager: React.FC<AlarmManagerProps> = ({ alarms, settings, addAlarm, updateAlarm, toggleAlarm, deleteAlarm, onPreview }) => {
  const [isAdding, setIsAdding] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [newTime, setNewTime] = useState('');
  const [selectedDays, setSelectedDays] = useState<number[]>([]);
  
  const [activeChallenges, setActiveChallenges] = useState<ChallengeConfig[]>([]);
  const [scanningId, setScanningId] = useState<string | null>(null);
  const [showDeviceList, setShowDeviceList] = useState<string | null>(null);

  const [wakeUpCheck, setWakeUpCheck] = useState<WakeUpCheckConfig>({
      enabled: false,
      checkDelay: 5,
      confirmWindow: 1
  });

  const [emergencyConfig, setEmergencyConfig] = useState<EmergencyContactConfig>({
      enabled: false,
      contactName: '',
      contactNumber: '',
      method: 'SMS',
      message: 'I am not waking up to my alarm. Please help.',
      triggerDelay: 10
  });
  
  const [audioConfig, setAudioConfig] = useState<AlarmAudioConfig>({
      source: 'GENERATED',
      generatedType: 'CLASSIC',
      systemType: 'MARIMBA'
  });

  const handleEdit = (alarm: Alarm) => {
      setNewTime(alarm.time);
      setSelectedDays(alarm.days);
      setActiveChallenges(alarm.challenges ? JSON.parse(JSON.stringify(alarm.challenges)) : []);
      
      if (alarm.wakeUpCheck) setWakeUpCheck(alarm.wakeUpCheck);
      else setWakeUpCheck({ enabled: false, checkDelay: 5, confirmWindow: 1 });

      if (alarm.emergencyContact) setEmergencyConfig(alarm.emergencyContact);
      else setEmergencyConfig({ 
            enabled: false, 
            contactName: '', 
            contactNumber: '', 
            method: 'SMS', 
            message: 'I am not waking up to my alarm. Please help.', 
            triggerDelay: 10 
      });

      if (alarm.audio) setAudioConfig(alarm.audio);
      else setAudioConfig({ source: 'GENERATED', generatedType: 'CLASSIC', systemType: 'MARIMBA' });

      setEditingId(alarm.id);
      setIsAdding(true);
      window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const finalTime = newTime || '07:00';
    if (finalTime) {
      triggerHaptic('success');
      if (editingId) {
          const existing = alarms.find(a => a.id === editingId);
          if (existing) {
              const updatedAlarm: Alarm = {
                  ...existing,
                  time: finalTime,
                  days: selectedDays,
                  challenges: activeChallenges,
                  wakeUpCheck: wakeUpCheck,
                  emergencyContact: emergencyConfig,
                  audio: audioConfig
              };
              updateAlarm(updatedAlarm);
          }
      } else {
          addAlarm(finalTime, selectedDays, activeChallenges, wakeUpCheck, emergencyConfig, audioConfig);
      }
      resetForm();
    }
  };

  const resetForm = () => {
      setNewTime('');
      setSelectedDays([]);
      setActiveChallenges([]);
      setWakeUpCheck({ enabled: false, checkDelay: 5, confirmWindow: 1 });
      setEmergencyConfig({ enabled: false, contactName: '', contactNumber: '', method: 'SMS', message: 'I am not waking up.', triggerDelay: 10 });
      setAudioConfig({ source: 'GENERATED', generatedType: 'CLASSIC', systemType: 'MARIMBA' });
      setEditingId(null);
      setIsAdding(false);
      setShowDeviceList(null);
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (file) {
          if (file.size > 2.5 * 1024 * 1024) {
              alert("File too large. Please choose a file under 2.5MB.");
              return;
          }
          const reader = new FileReader();
          reader.onload = (evt) => {
              if (evt.target?.result) {
                  setAudioConfig({
                      source: 'FILE',
                      fileData: evt.target.result as string,
                      fileName: file.name
                  });
              }
          };
          reader.readAsDataURL(file);
      }
  };

  const toggleDay = (dayIndex: number) => {
    triggerHaptic('selection');
    setSelectedDays(prev => 
      prev.includes(dayIndex) 
        ? prev.filter(d => d !== dayIndex) 
        : [...prev, dayIndex].sort()
    );
  };

  const addChallenge = (type: ChallengeType) => {
      triggerHaptic('light');
      const config: ChallengeConfig = {
          id: uuidv4(),
          type,
          params: {
              count: type === 'BURST' ? 20 : type === 'MEMORY' ? 5 : type === 'MATH' ? 3 : type === 'TYPING' ? 3 : undefined,
              rounds: type === 'MEMORY' ? 3 : undefined,
              difficulty: 'NORMAL',
              targetSpeed: type === 'VELOCITY' ? 10 : undefined,
              deviceName: ''
          }
      };
      setActiveChallenges(prev => [...prev, config]);
  };

  const removeChallenge = (id: string) => {
      triggerHaptic('medium');
      setActiveChallenges(prev => prev.filter(c => c.id !== id));
  };

  const updateChallengeParam = (id: string, key: string, value: any) => {
      setActiveChallenges(prev => prev.map(c => 
          c.id === id ? { ...c, params: { ...c.params, [key]: value } } : c
      ));
  };

  const handleScan = (id: string) => {
      triggerHaptic('medium');
      setScanningId(id);
      setTimeout(() => {
          setScanningId(null);
          setShowDeviceList(prev => prev === id ? null : id);
      }, 1200);
  };

  const selectDevice = (id: string, name: string) => {
      triggerHaptic('selection');
      updateChallengeParam(id, 'deviceName', name);
      setShowDeviceList(null);
  };
  
  const playPreview = () => {
      triggerHaptic('medium');
      audioService.playAlarm(audioConfig);
      const duration = audioConfig.source === 'SYSTEM' ? 4000 : (audioConfig.source === 'GENERATED' ? 2000 : 5000);
      setTimeout(() => audioService.stopAlarm(), duration);
  };

  const weekDays = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];

  const availableChallenges: { type: ChallengeType; icon: React.ReactNode; label: string }[] = [
    { type: 'MATH', icon: <Brain size={16} />, label: 'MATH' },
    { type: 'BURST', icon: <Zap size={16} />, label: 'BURST' },
    { type: 'MEMORY', icon: <Grid3X3 size={16} />, label: 'MEM' },
    { type: 'TYPING', icon: <Keyboard size={16} />, label: 'TYPE' },
    { type: 'BLUETOOTH', icon: <Bluetooth size={16} />, label: 'LINK' },
    { type: 'VELOCITY', icon: <Activity size={16} />, label: 'RUN' },
  ];

  const renderChallengeConfig = (challenge: ChallengeConfig) => {
    const inputClass = "bg-surfaceHover border-2 border-black px-2 py-1.5 text-sm font-bold font-display text-textMain outline-none w-full text-center shadow-[2px_2px_0px_0px_#000] focus:shadow-none focus:translate-x-[2px] focus:translate-y-[2px] transition-all placeholder:text-textMuted/30";
    const selectClass = "bg-surfaceHover border-2 border-black px-2 py-1.5 text-sm font-bold font-display text-textMain outline-none w-full appearance-none text-center cursor-pointer shadow-[2px_2px_0px_0px_#000] focus:shadow-none focus:translate-x-[2px] focus:translate-y-[2px] transition-all";

    switch (challenge.type) {
      case 'BURST':
        return <ConfigGroup label="Taps"><input type="number" value={challenge.params.count} onChange={(e) => updateChallengeParam(challenge.id, 'count', parseInt(e.target.value))} className={inputClass} /></ConfigGroup>;
      case 'MATH':
        return (
            <>
                <ConfigGroup label="Count"><input type="number" value={challenge.params.count} onChange={(e) => updateChallengeParam(challenge.id, 'count', parseInt(e.target.value))} className={inputClass} /></ConfigGroup>
                <ConfigGroup label="Diff"><select value={challenge.params.difficulty} onChange={(e) => updateChallengeParam(challenge.id, 'difficulty', e.target.value)} className={selectClass}><option value="NORMAL">Normal</option><option value="HARD">Hard</option></select></ConfigGroup>
            </>
        );
      case 'TYPING':
        return (
            <>
              <ConfigGroup label="Phrases"><input type="number" value={challenge.params.count} onChange={(e) => updateChallengeParam(challenge.id, 'count', parseInt(e.target.value))} className={inputClass} /></ConfigGroup>
              <ConfigGroup label="Mode"><select value={challenge.params.difficulty} onChange={(e) => updateChallengeParam(challenge.id, 'difficulty', e.target.value)} className={selectClass}><option value="NORMAL">Simple</option><option value="HARD">Quotes</option></select></ConfigGroup>
          </>
        );
      case 'MEMORY':
        return (
            <>
              <ConfigGroup label="Rounds"><input type="number" value={challenge.params.rounds} onChange={(e) => updateChallengeParam(challenge.id, 'rounds', parseInt(e.target.value))} className={inputClass} /></ConfigGroup>
              <ConfigGroup label="Len"><input type="number" value={challenge.params.count} onChange={(e) => updateChallengeParam(challenge.id, 'count', parseInt(e.target.value))} className={inputClass} /></ConfigGroup>
          </>
        );
      case 'BLUETOOTH':
        return (
           <ConfigGroup label="Device">
             <div className="flex flex-col gap-2 w-full">
                <div className="flex gap-2">
                    <input type="text" placeholder="Scan Device" value={challenge.params.deviceName || ''} readOnly onClick={() => handleScan(challenge.id)} className={`${inputClass} text-left cursor-pointer`} />
                    <button type="button" onClick={() => handleScan(challenge.id)} className="bg-surfaceHover border-2 border-black px-3 text-textMain hover:bg-accent hover:text-white transition-colors shadow-[2px_2px_0px_0px_#000] active:shadow-none active:translate-x-[2px] active:translate-y-[2px]">{scanningId === challenge.id ? <Loader2 className="animate-spin" size={16} /> : <Search size={16} />}</button>
                </div>
                <AnimatePresence>
                {showDeviceList === challenge.id && (
                    <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }} className="bg-surface border-2 border-black shadow-hard overflow-hidden">
                        <div className="p-1 space-y-1">
                            {['Bedroom Speaker', 'Smart TV', 'Headphones', 'Car Console'].map(dev => (
                                <button key={dev} type="button" onClick={() => selectDevice(challenge.id, dev)} className="w-full text-left px-3 py-2 text-sm font-bold font-display text-textMain hover:bg-accent hover:text-white flex items-center justify-between group transition-colors border border-transparent hover:border-black">
                                    <div className="flex items-center gap-2"><Smartphone size={14} /> {dev}</div>
                                </button>
                            ))}
                        </div>
                    </motion.div>
                )}
                </AnimatePresence>
             </div>
          </ConfigGroup>
        );
      case 'VELOCITY':
        return <ConfigGroup label="Speed (km/h)"><input type="number" value={challenge.params.targetSpeed} onChange={(e) => updateChallengeParam(challenge.id, 'targetSpeed', parseInt(e.target.value))} className={inputClass} /></ConfigGroup>;
      default: return null;
    }
  };

  return (
    <div className="w-full max-w-2xl mx-auto z-10 relative px-4 pb-32">
      
      <div className="flex justify-center mb-8">
        <motion.button
          whileHover={{ x: -2, y: -2, boxShadow: '6px 6px 0px 0px #000' }}
          whileTap={{ x: 0, y: 0, boxShadow: '0px 0px 0px 0px #000', scale: 0.98 }}
          onClick={() => {
              triggerHaptic('medium');
              if (isAdding) {
                resetForm();
              } else {
                setNewTime(new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }));
                setIsAdding(true);
              }
          }}
          className={`
            flex items-center gap-2 px-8 py-3 font-bold font-display text-lg border-2 border-black shadow-hard transition-all active:shadow-none active:translate-x-1 active:translate-y-1
            ${isAdding ? 'bg-surface text-textMain' : 'bg-accent text-accentText hover:bg-accentHover'}
          `}
        >
          {isAdding ? <X size={20} strokeWidth={3} /> : <Plus size={20} strokeWidth={3} />}
          <span>{isAdding ? 'CANCEL' : 'NEW ALARM'}</span>
        </motion.button>
      </div>

      <AnimatePresence>
        {isAdding && (
          <motion.form
            initial={{ opacity: 0, height: 0, scale: 0.95 }}
            animate={{ opacity: 1, height: 'auto', scale: 1 }}
            exit={{ opacity: 0, height: 0, scale: 0.95 }}
            transition={{ type: "spring", bounce: 0.3 }}
            className="mb-8 overflow-hidden p-2"
            onSubmit={handleSubmit}
          >
            <div className="p-6 bg-surface border-2 border-black shadow-hard space-y-8">

                {/* Time & Days */}
                <div className="flex flex-col items-center gap-6">
                     {/* Custom Wheel Picker */}
                     <TimePickerWheel 
                        value={newTime || '07:00'} 
                        onChange={setNewTime} 
                        format={settings.timeFormat} 
                     />

                     {/* Time Until Badge */}
                     <motion.div 
                        initial={{ opacity: 0, y: -5 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="flex items-center gap-2 px-3 py-1 bg-accent/10 border-2 border-black rounded-full"
                     >
                        <Clock size={14} className="text-accent" />
                        <span className="text-xs font-bold font-display text-textMain uppercase tracking-wide">
                            {getTimeUntil(newTime || '07:00', selectedDays)}
                        </span>
                     </motion.div>
                     
                     <div className="flex gap-2">
                        {weekDays.map((day, index) => (
                            <button
                                key={index}
                                type="button"
                                onClick={() => toggleDay(index)}
                                className={`
                                    w-10 h-10 border-2 border-black flex items-center justify-center font-bold font-display text-sm transition-all
                                    ${selectedDays.includes(index) 
                                        ? 'bg-textMain text-bg shadow-[2px_2px_0px_0px_#000] -translate-y-0.5' 
                                        : 'bg-surface hover:bg-surfaceHover text-textMuted shadow-[2px_2px_0px_0px_#000] active:shadow-none active:translate-y-[2px] active:translate-x-[2px]'}
                                `}
                            >
                                {day}
                            </button>
                        ))}
                     </div>
                </div>

                <div className="h-0.5 bg-black w-full" />

                {/* Challenges Selection */}
                <div>
                    <h3 className="text-sm font-bold font-display uppercase tracking-wider mb-4 text-center bg-black text-white inline-block px-2 transform -rotate-1 mx-auto block w-fit">Challenges</h3>
                    <div className="flex flex-wrap justify-center gap-3 mb-6">
                        {availableChallenges.map((c) => (
                            <button
                                key={c.type}
                                type="button"
                                onClick={() => addChallenge(c.type)}
                                className="flex items-center gap-1.5 px-3 py-2 bg-surfaceHover border-2 border-black shadow-[2px_2px_0px_0px_#000] active:shadow-none active:translate-y-[2px] active:translate-x-[2px] text-textMain text-xs font-bold font-display transition-all"
                            >
                                <span className="text-accent">{c.icon}</span>
                                {c.label}
                            </button>
                        ))}
                    </div>

                    <div className="space-y-4">
                        <AnimatePresence>
                            {activeChallenges.map((challenge, index) => (
                                <motion.div 
                                    key={challenge.id}
                                    initial={{ opacity: 0, x: -10 }}
                                    animate={{ opacity: 1, x: 0 }}
                                    exit={{ opacity: 0, scale: 0.95 }}
                                    layout
                                    className="bg-surface p-4 border-2 border-black flex flex-col gap-4 relative shadow-[4px_4px_0px_0px_#000]"
                                >
                                    <div className="flex items-center justify-between">
                                         <div className="flex items-center gap-3 min-w-0">
                                             <div className="w-8 h-8 flex-shrink-0 border-2 border-black bg-surfaceHover flex items-center justify-center text-textMain shadow-[2px_2px_0px_0px_#000]">
                                                {availableChallenges.find(c => c.type === challenge.type)?.icon}
                                             </div>
                                             <span className="text-sm font-bold font-display uppercase text-textMain truncate">
                                                 {availableChallenges.find(c => c.type === challenge.type)?.label} PROTOCOL
                                             </span>
                                         </div>
                                         <div className="flex items-center gap-2 flex-shrink-0">
                                             <button type="button" onClick={() => { triggerHaptic('light'); onPreview([challenge]); }} className="p-1.5 border-2 border-black bg-surfaceHover text-textMain hover:bg-accent hover:text-white transition-colors shadow-[2px_2px_0px_0px_#000] active:shadow-none active:translate-x-[2px] active:translate-y-[2px]"><Play size={16} /></button>
                                             <button type="button" onClick={() => removeChallenge(challenge.id)} className="p-1.5 border-2 border-black bg-surfaceHover text-textMain hover:bg-red-500 hover:text-white transition-colors shadow-[2px_2px_0px_0px_#000] active:shadow-none active:translate-x-[2px] active:translate-y-[2px]"><Trash2 size={16} /></button>
                                         </div>
                                    </div>
                                    
                                    <div className="flex flex-wrap gap-4 items-end">
                                        {renderChallengeConfig(challenge)}
                                    </div>
                                    
                                    <div className="absolute -left-3 -top-3 w-6 h-6 border-2 border-black bg-accent flex items-center justify-center text-xs text-white font-bold z-10 shadow-hard-sm">
                                        {index + 1}
                                    </div>
                                </motion.div>
                            ))}
                        </AnimatePresence>
                    </div>
                </div>

                <div className="h-0.5 bg-black w-full" />
                
                {/* Audio Configuration */}
                <div>
                     <h3 className="text-sm font-bold font-display uppercase tracking-wider mb-4 text-center bg-black text-white inline-block px-2 transform -rotate-1 mx-auto block w-fit">Audio Source</h3>
                     
                     <div className="grid grid-cols-4 gap-2 mb-4">
                        {(['GENERATED', 'SYSTEM', 'URL', 'FILE'] as const).map(source => (
                            <button
                                key={source}
                                type="button"
                                onClick={() => { triggerHaptic('medium'); setAudioConfig(prev => ({ ...prev, source })); }}
                                className={`
                                    py-2 px-1 border-2 border-black font-bold font-display text-[10px] sm:text-xs flex flex-col items-center gap-1 transition-all
                                    ${audioConfig.source === source 
                                        ? 'bg-accent text-white shadow-[2px_2px_0px_0px_#000] -translate-y-0.5' 
                                        : 'bg-surface hover:bg-surfaceHover text-textMain shadow-[2px_2px_0px_0px_#000] active:translate-y-[2px] active:shadow-none'}
                                `}
                            >
                                {source === 'GENERATED' && <Zap size={16} />}
                                {source === 'SYSTEM' && <Smartphone size={16} />}
                                {source === 'URL' && <Radio size={16} />}
                                {source === 'FILE' && <Upload size={16} />}
                                {source === 'GENERATED' ? 'NOISE' : (source === 'SYSTEM' ? 'OS' : source)}
                            </button>
                        ))}
                     </div>

                     <AnimatePresence mode="wait">
                         {audioConfig.source === 'GENERATED' && (
                             <motion.div 
                                initial={{ opacity: 0, y: -5 }} animate={{ opacity: 1, y: 0 }}
                                className="space-y-3"
                             >
                                 <div className="grid grid-cols-4 gap-2">
                                     {(['CLASSIC', 'DIGITAL', 'ZEN', 'HAZARD'] as const).map(type => (
                                         <button
                                            key={type}
                                            type="button"
                                            onClick={() => { triggerHaptic('light'); setAudioConfig(prev => ({...prev, generatedType: type})) }}
                                            className={`
                                                py-2 text-[10px] font-bold border-2 border-black transition-all
                                                ${audioConfig.generatedType === type ? 'bg-textMain text-bg' : 'bg-surface hover:bg-surfaceHover text-textMuted'}
                                            `}
                                         >
                                             {type}
                                         </button>
                                     ))}
                                 </div>
                                 <button type="button" onClick={playPreview} className="w-full flex items-center justify-center gap-2 bg-surfaceHover border-2 border-black py-2 text-xs font-bold text-textMain hover:bg-accent hover:text-white transition-colors shadow-hard-sm active:translate-y-[2px] active:shadow-none">
                                     <Volume2 size={14} /> PREVIEW SOUND
                                 </button>
                             </motion.div>
                         )}
                         {audioConfig.source === 'SYSTEM' && (
                             <motion.div 
                                initial={{ opacity: 0, y: -5 }} animate={{ opacity: 1, y: 0 }}
                                className="space-y-3"
                             >
                                 <div className="grid grid-cols-4 gap-2">
                                     {(['MARIMBA', 'COSMIC', 'RIPPLE', 'CIRCUIT'] as const).map(type => (
                                         <button
                                            key={type}
                                            type="button"
                                            onClick={() => { triggerHaptic('light'); setAudioConfig(prev => ({...prev, systemType: type})) }}
                                            className={`
                                                py-2 text-[10px] font-bold border-2 border-black transition-all
                                                ${audioConfig.systemType === type ? 'bg-textMain text-bg' : 'bg-surface hover:bg-surfaceHover text-textMuted'}
                                            `}
                                         >
                                             {type}
                                         </button>
                                     ))}
                                 </div>
                                 <button type="button" onClick={playPreview} className="w-full flex items-center justify-center gap-2 bg-surfaceHover border-2 border-black py-2 text-xs font-bold text-textMain hover:bg-accent hover:text-white transition-colors shadow-hard-sm active:translate-y-[2px] active:shadow-none">
                                     <Volume2 size={14} /> PLAY MELODY
                                 </button>
                             </motion.div>
                         )}
                         {audioConfig.source === 'URL' && (
                             <motion.div initial={{ opacity: 0, y: -5 }} animate={{ opacity: 1, y: 0 }} className="space-y-3">
                                <input 
                                    type="url"
                                    placeholder="https://stream.radio.com/jazz.mp3" 
                                    className="w-full bg-surfaceHover p-3 border-2 border-black text-sm font-bold placeholder:text-textMuted/30 outline-none shadow-[2px_2px_0px_0px_#000] text-textMain"
                                    value={audioConfig.url || ''}
                                    onChange={e => setAudioConfig(prev => ({ ...prev, url: e.target.value }))}
                                />
                                <button type="button" onClick={playPreview} className="w-full flex items-center justify-center gap-2 bg-surfaceHover border-2 border-black py-2 text-xs font-bold text-textMain hover:bg-accent hover:text-white transition-colors shadow-hard-sm active:translate-y-[2px] active:shadow-none">
                                     <Volume2 size={14} /> TEST LINK
                                 </button>
                             </motion.div>
                         )}
                         {audioConfig.source === 'FILE' && (
                             <motion.div initial={{ opacity: 0, y: -5 }} animate={{ opacity: 1, y: 0 }} className="flex flex-col gap-2">
                                <label className="flex items-center gap-2 w-full bg-surfaceHover p-3 border-2 border-black cursor-pointer shadow-[2px_2px_0px_0px_#000] active:translate-y-[2px] active:shadow-none transition-all hover:bg-surface">
                                    <div className="bg-black text-white p-1"><Upload size={14} /></div>
                                    <span className="text-xs font-bold text-textMain truncate flex-1">
                                        {audioConfig.fileName || 'SELECT MP3/WAV (MAX 2.5MB)'}
                                    </span>
                                    <input type="file" accept="audio/*" className="hidden" onChange={handleFileUpload} />
                                </label>
                                {audioConfig.fileData && (
                                    <>
                                        <div className="text-[10px] text-textMuted font-bold text-center uppercase tracking-wide">
                                            File Loaded Ready
                                        </div>
                                        <button type="button" onClick={playPreview} className="w-full flex items-center justify-center gap-2 bg-surfaceHover border-2 border-black py-2 text-xs font-bold text-textMain hover:bg-accent hover:text-white transition-colors shadow-hard-sm active:translate-y-[2px] active:shadow-none">
                                             <Volume2 size={14} /> PLAY FILE
                                         </button>
                                    </>
                                )}
                             </motion.div>
                         )}
                     </AnimatePresence>
                </div>

                <div className="h-0.5 bg-black w-full" />

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                     <div className={`p-4 border-2 border-black transition-all shadow-hard-sm ${wakeUpCheck.enabled ? 'bg-accent/10' : 'bg-surface'}`}>
                        <div className="flex justify-between items-center mb-3">
                             <div className="flex items-center gap-2 text-textMain font-bold font-display text-sm"><Eye size={16} /> REALITY CHECK</div>
                             <button type="button" onClick={() => { triggerHaptic('medium'); setWakeUpCheck(prev => ({...prev, enabled: !prev.enabled})); }} className={`w-10 h-6 border-2 border-black flex items-center px-0.5 transition-colors shadow-[2px_2px_0px_0px_#000] active:shadow-none active:translate-x-[2px] active:translate-y-[2px] ${wakeUpCheck.enabled ? 'bg-accent' : 'bg-white'}`}><motion.div animate={{ x: wakeUpCheck.enabled ? 16 : 0 }} className="w-4 h-4 bg-white border-2 border-black" /></button>
                        </div>
                        {wakeUpCheck.enabled && (
                            <div className="space-y-3 pt-3 border-t-2 border-black/10">
                                <div className="flex justify-between items-center text-[10px] font-bold text-textMuted uppercase tracking-wide"><span>Delay (min)</span><input type="number" value={wakeUpCheck.checkDelay} onChange={e => setWakeUpCheck(p => ({...p, checkDelay: parseInt(e.target.value) || 1}))} className="w-14 bg-surfaceHover border-2 border-black p-1 text-center font-bold shadow-[2px_2px_0px_0px_#000] text-textMain" /></div>
                                <div className="flex justify-between items-center text-[10px] font-bold text-textMuted uppercase tracking-wide"><span>Window (min)</span><input type="number" value={wakeUpCheck.confirmWindow} onChange={e => setWakeUpCheck(p => ({...p, confirmWindow: parseInt(e.target.value) || 1}))} className="w-14 bg-surfaceHover border-2 border-black p-1 text-center font-bold shadow-[2px_2px_0px_0px_#000] text-textMain" /></div>
                            </div>
                        )}
                     </div>

                     <div className={`p-4 border-2 border-black transition-all shadow-hard-sm ${emergencyConfig.enabled ? 'bg-red-500/10' : 'bg-surface'}`}>
                        <div className="flex justify-between items-center mb-3">
                             <div className="flex items-center gap-2 text-textMain font-bold font-display text-sm"><Siren size={16} /> SOS CONTACT</div>
                             <button type="button" onClick={() => { triggerHaptic('medium'); setEmergencyConfig(prev => ({...prev, enabled: !prev.enabled})); }} className={`w-10 h-6 border-2 border-black flex items-center px-0.5 transition-colors shadow-[2px_2px_0px_0px_#000] active:shadow-none active:translate-x-[2px] active:translate-y-[2px] ${emergencyConfig.enabled ? 'bg-red-500' : 'bg-white'}`}><motion.div animate={{ x: emergencyConfig.enabled ? 16 : 0 }} className="w-4 h-4 bg-white border-2 border-black" /></button>
                        </div>
                         {emergencyConfig.enabled && (
                            <div className="space-y-2 pt-3 border-t-2 border-black/10">
                                <input placeholder="NAME" className="w-full bg-surfaceHover p-1.5 border-2 border-black text-sm font-bold placeholder:text-textMuted/50 outline-none shadow-[2px_2px_0px_0px_#000] text-textMain" value={emergencyConfig.contactName} onChange={e => setEmergencyConfig(p => ({...p, contactName: e.target.value}))}/>
                                <input placeholder="NUMBER" className="w-full bg-surfaceHover p-1.5 border-2 border-black text-sm font-bold placeholder:text-textMuted/50 outline-none shadow-[2px_2px_0px_0px_#000] text-textMain" value={emergencyConfig.contactNumber} onChange={e => setEmergencyConfig(p => ({...p, contactNumber: e.target.value}))}/>
                            </div>
                        )}
                     </div>
                </div>

                <button type="submit" className="w-full py-4 bg-textMain text-bg border-2 border-black font-display font-bold text-xl shadow-hard hover:shadow-hard-hover active:shadow-none active:translate-x-1 active:translate-y-1 transition-all">
                    {editingId ? 'UPDATE ALARM' : 'SAVE ALARM'}
                </button>
            </div>
          </motion.form>
        )}
      </AnimatePresence>

      <motion.div 
        className="space-y-4 min-h-[200px]"
        initial="hidden"
        animate="show"
        variants={{
            hidden: { opacity: 0 },
            show: {
                opacity: 1,
                transition: { staggerChildren: 0.1 }
            }
        }}
      >
        <AnimatePresence mode="popLayout">
            {alarms.map(alarm => (
                <AlarmItem key={alarm.id} alarm={alarm} settings={settings} onToggle={toggleAlarm} onDelete={deleteAlarm} onEdit={handleEdit} />
            ))}
        </AnimatePresence>
        
        {alarms.length === 0 && !isAdding && (
            <motion.div 
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="flex flex-col items-center justify-center py-20 opacity-60"
            >
                <motion.div 
                    animate={{ y: [0, -10, 0], rotate: [0, 5, -5, 0] }}
                    transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
                    className="w-24 h-24 border-2 border-black bg-surface mb-6 flex items-center justify-center shadow-hard rounded-full"
                >
                    <Ghost size={40} className="text-textMain" />
                </motion.div>
                <span className="font-display font-bold text-textMuted text-lg uppercase tracking-widest bg-surface px-4 py-1 border-2 border-transparent">No Active Protocols</span>
            </motion.div>
        )}
      </motion.div>
    </div>
  );
};
