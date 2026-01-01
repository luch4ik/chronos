
import React from 'react';
import { motion } from 'framer-motion';
import { Trash2, Zap, Brain, Grid3X3, Keyboard, Bell, Bluetooth, Activity, Eye, Siren, Music, Radio, Upload, FileAudio, Smartphone } from 'lucide-react';
import { Alarm, AppSettings } from '../types';
import { formatTime, triggerHaptic } from '../utils';

interface AlarmItemProps {
  alarm: Alarm;
  settings: AppSettings;
  onToggle: (id: string) => void;
  onDelete: (id: string) => void;
  onEdit: (alarm: Alarm) => void;
}

export const AlarmItem: React.FC<AlarmItemProps> = ({ alarm, settings, onToggle, onDelete, onEdit }) => {
  const getScheduleString = (days: number[]) => {
    if (days.length === 0) return 'ONCE';
    if (days.length === 7) return 'DAILY';
    if (days.length === 5 && days.every(d => d >= 1 && d <= 5)) return 'WEEKDAYS';
    if (days.length === 2 && days.includes(0) && days.includes(6)) return 'WEEKENDS';
    
    const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    return days.map(d => dayNames[d]).join(' ');
  };

  const getChallengeIcon = (type: string, index: number) => {
    const props = { size: 16, className: "text-textMain" };
    switch (type) {
        case 'MATH': return <Brain key={index} {...props} />;
        case 'BURST': return <Zap key={index} {...props} />;
        case 'MEMORY': return <Grid3X3 key={index} {...props} />;
        case 'TYPING': return <Keyboard key={index} {...props} />;
        case 'BLUETOOTH': return <Bluetooth key={index} {...props} />;
        case 'VELOCITY': return <Activity key={index} {...props} />;
        default: return <Bell key={index} {...props} />;
    }
  };

  const getAudioIcon = () => {
      if (!alarm.audio || alarm.audio.source === 'GENERATED') return null;
      if (alarm.audio.source === 'SYSTEM') return <Smartphone size={16} />;
      if (alarm.audio.source === 'URL') return <Radio size={16} />;
      if (alarm.audio.source === 'FILE') return <FileAudio size={16} />;
      return null;
  }

  return (
    <motion.div
      layout
      variants={{
          hidden: { opacity: 0, y: 20, scale: 0.95 },
          show: { opacity: 1, y: 0, scale: 1 }
      }}
      exit={{ opacity: 0, scale: 0.9, transition: { duration: 0.2 } }}
      onClick={() => {
          triggerHaptic('selection');
          onEdit(alarm);
      }}
      drag="x"
      dragConstraints={{ left: 0, right: 0 }}
      whileHover={{ scale: 1.02, y: -4, zIndex: 10, boxShadow: '8px 8px 0px 0px #000' }}
      whileTap={{ scale: 0.98, boxShadow: '2px 2px 0px 0px #000', y: 0 }}
      className={`
        group relative flex items-center justify-between p-5 mb-4 border-2 border-black cursor-pointer transition-colors duration-200 select-none
        ${alarm.isActive ? 'bg-surface shadow-hard' : 'bg-surface/50 border-textMuted shadow-none opacity-80'}
      `}
    >
      <div className="flex flex-col gap-1.5 pointer-events-none min-w-0 flex-1">
        <div className={`text-5xl font-display font-black tracking-tighter ${alarm.isActive ? 'text-textMain' : 'text-textMuted'}`}>
            {formatTime(alarm.time, settings.timeFormat)}
        </div>
        
        <div className="flex items-center gap-3 flex-wrap">
            <span className={`
                text-[10px] font-black font-display uppercase px-2 py-0.5 border border-black whitespace-nowrap
                ${alarm.isActive ? 'bg-textMain text-bg' : 'bg-transparent text-textMuted border-textMuted'}
            `}>
                {getScheduleString(alarm.days)}
            </span>
            
            <div className="flex gap-1.5 pl-1 flex-wrap">
                {alarm.challenges && alarm.challenges.map((c, idx) => (
                    <div key={idx} className="bg-surfaceHover border border-black p-0.5" title={`${c.type} challenge`}>
                        {getChallengeIcon(c.type, idx)}
                    </div>
                ))}
                {alarm.wakeUpCheck?.enabled && (
                    <div className="bg-accent/20 border border-black p-0.5 text-accent">
                        <Eye size={16} />
                    </div>
                )}
                {alarm.emergencyContact?.enabled && (
                    <div className="bg-red-500/20 border border-black p-0.5 text-red-500">
                        <Siren size={16} />
                    </div>
                )}
                {alarm.audio && alarm.audio.source !== 'GENERATED' && (
                    <div className="bg-surfaceHover border border-black p-0.5 text-textMain" title={alarm.audio.source}>
                        {getAudioIcon()}
                    </div>
                )}
            </div>
        </div>
      </div>

      <div className="flex items-center gap-5 relative z-20 flex-shrink-0 ml-4" onClick={e => e.stopPropagation()}>
        {/* Reactive Brutalist Toggle */}
        <button 
            onClick={(e) => { 
                e.stopPropagation(); 
                triggerHaptic('medium');
                onToggle(alarm.id); 
            }}
            className={`
                w-14 h-8 border-2 border-black flex items-center px-1 transition-colors duration-300
                ${alarm.isActive ? 'bg-accent' : 'bg-surfaceHover'}
            `}
        >
            <motion.div 
                className="w-5 h-5 border-2 border-black bg-white"
                animate={{ x: alarm.isActive ? 24 : 0, rotate: alarm.isActive ? 90 : 0 }}
                transition={{ type: "spring", stiffness: 300, damping: 20 }}
            />
        </button>

        <motion.button 
            whileHover={{ scale: 1.1, rotate: 10, backgroundColor: '#ef4444', color: '#fff', borderColor: '#000' }}
            whileTap={{ scale: 0.9 }}
            onClick={(e) => { 
                e.stopPropagation(); 
                triggerHaptic('heavy');
                onDelete(alarm.id); 
            }}
            className="p-2 border-2 border-transparent hover:border-black text-textMuted transition-all rounded-sm"
        >
            <Trash2 size={20} />
        </motion.button>
      </div>
    </motion.div>
  );
};
