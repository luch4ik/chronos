import React from 'react';
import { motion } from 'framer-motion';
import { X, Shield, Volume2, PowerOff, Clock, Moon, Sun, Monitor } from 'lucide-react';
import { AppSettings } from '../types';
import { triggerHaptic } from '../utils';

interface SettingsModalProps {
  settings: AppSettings;
  onUpdate: (newSettings: AppSettings) => void;
  onClose: () => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({ settings, onUpdate, onClose }) => {
  
  const toggleTheme = () => {
      const themes: AppSettings['theme'][] = ['light', 'dark', 'system'];
      const currentIdx = themes.indexOf(settings.theme);
      const nextTheme = themes[(currentIdx + 1) % themes.length];
      onUpdate({ ...settings, theme: nextTheme });
  };

  const getThemeIcon = () => {
      if (settings.theme === 'light') return Sun;
      if (settings.theme === 'dark') return Moon;
      return Monitor;
  };
  
  const getThemeLabel = () => {
       if (settings.theme === 'light') return "LIGHT";
       if (settings.theme === 'dark') return "DARK";
       return "AUTO";
  };

  const toggleSetting = (key: keyof AppSettings) => {
      triggerHaptic('medium');
      if (key === 'timeFormat') onUpdate({ ...settings, timeFormat: settings.timeFormat === '24h' ? '12h' : '24h' });
      else if (key === 'theme') toggleTheme();
      else {
          // @ts-ignore
          onUpdate({ ...settings, [key]: !settings[key] });
      }
  };

  const OptionItem = ({ label, desc, active, onClick, icon: Icon, isToggle = true }: any) => (
      <button 
        onClick={onClick}
        className="w-full flex items-center justify-between p-4 border-b-2 border-black hover:bg-surfaceHover transition-all text-left group active:bg-accent/10"
      >
          <div className="flex items-center gap-4">
              <div className={`p-2 border-2 border-black transition-colors ${active ? 'bg-textMain text-bg' : 'bg-surface text-textMain'}`}>
                  <Icon size={20} strokeWidth={2.5} />
              </div>
              <div>
                  <div className="font-bold font-display text-sm text-textMain uppercase">{label}</div>
                  <div className="text-xs font-bold text-textMuted mt-0.5">{desc}</div>
              </div>
          </div>
          {isToggle ? (
            <div className={`w-12 h-6 border-2 border-black relative transition-colors duration-200 ${active ? 'bg-accent' : 'bg-surface'}`}>
                <motion.div 
                    animate={{ x: active ? 22 : -2 }} 
                    className="absolute top-[-2px] left-[-2px] w-6 h-6 bg-white border-2 border-black shadow-hard-sm" 
                />
            </div>
          ) : (
            <div className="px-2 py-1 border-2 border-black bg-white text-xs font-black text-textMain uppercase">
                {active}
            </div>
          )}
      </button>
  );

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
        <motion.div 
            initial={{ opacity: 0 }} 
            animate={{ opacity: 1 }} 
            exit={{ opacity: 0 }}
            className="absolute inset-0 bg-accent/20 backdrop-blur-sm" 
            onClick={() => { triggerHaptic('light'); onClose(); }}
        />
        <motion.div 
            initial={{ scale: 0.95, opacity: 0, y: 20 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.95, opacity: 0, y: 20 }}
            className="bg-surface border-4 border-black shadow-hard w-full max-w-md relative max-h-[85vh] overflow-y-auto"
        >
            <div className="flex justify-between items-center p-6 border-b-4 border-black bg-surface sticky top-0 z-10">
                <h2 className="text-2xl font-black font-display text-textMain uppercase italic">Settings</h2>
                <button onClick={() => { triggerHaptic('light'); onClose(); }} className="p-2 border-2 border-black hover:bg-red-500 hover:text-white transition-colors shadow-hard-sm active:translate-y-1 active:shadow-none">
                    <X size={24} strokeWidth={3} />
                </button>
            </div>

            <div className="p-0">
                <div className="bg-textMain text-bg p-2 text-xs font-bold uppercase tracking-widest text-center border-b-2 border-black">Appearance</div>
                <OptionItem 
                    icon={getThemeIcon()}
                    label="Theme"
                    desc="Color Scheme"
                    active={getThemeLabel()}
                    onClick={() => toggleSetting('theme')}
                    isToggle={false}
                />
                 <OptionItem 
                    icon={Clock}
                    label="24-Hour Time"
                    desc="Military Format"
                    active={settings.timeFormat === '24h'}
                    onClick={() => toggleSetting('timeFormat')}
                />
                
                <div className="bg-textMain text-bg p-2 text-xs font-bold uppercase tracking-widest text-center border-y-2 border-black">System</div>
                <OptionItem 
                    icon={Shield}
                    label="Hardcore Mode"
                    desc="Block Uninstall"
                    active={settings.uninstallProtection}
                    onClick={() => toggleSetting('uninstallProtection')}
                />
                 <OptionItem 
                    icon={Volume2}
                    label="Max Volume"
                    desc="Override System"
                    active={settings.volumeOverride}
                    onClick={() => toggleSetting('volumeOverride')}
                />
                 <OptionItem 
                    icon={PowerOff}
                    label="Auto Reboot"
                    desc="Persistence"
                    active={settings.rebootProtection}
                    onClick={() => toggleSetting('rebootProtection')}
                />
            </div>
            
            <div className="p-6 text-center bg-bg border-t-2 border-black">
                <div className="text-xs font-bold text-textMuted uppercase tracking-widest">Chronos v3.0 // BUILD 9021</div>
            </div>
        </motion.div>
    </div>
  );
};