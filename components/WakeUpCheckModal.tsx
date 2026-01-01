import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Eye } from 'lucide-react';
import { triggerHaptic } from '../utils';

interface WakeUpCheckModalProps {
  confirmWindowMinutes: number;
  onConfirm: () => void;
  deadline: number;
}

export const WakeUpCheckModal: React.FC<WakeUpCheckModalProps> = ({ confirmWindowMinutes, onConfirm, deadline }) => {
  const [progress, setProgress] = useState(100);

  useEffect(() => {
    const totalDuration = confirmWindowMinutes * 60 * 1000;
    const interval = setInterval(() => {
      const diff = deadline - Date.now();
      if (diff <= 0) setProgress(0);
      else setProgress(100 - ((totalDuration - diff) / totalDuration) * 100);
    }, 100);
    return () => clearInterval(interval);
  }, [deadline, confirmWindowMinutes]);

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-accent/30 backdrop-blur-sm" />
      <motion.div 
        initial={{ scale: 0.9, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        className="relative bg-surface border-4 border-black shadow-hard w-full max-w-sm p-8 text-center"
      >
        <div className="w-16 h-16 border-2 border-black bg-white flex items-center justify-center mx-auto mb-6 shadow-hard-sm">
            <Eye size={32} className="text-black" />
        </div>
        <h2 className="text-2xl font-black font-display text-textMain mb-2 uppercase">Reality Check</h2>
        <p className="text-textMuted font-bold text-sm mb-6 border-2 border-black p-2 bg-white">CONFIRM PRESENCE OR ALARM RESUMES</p>

        <div className="h-4 bg-white border-2 border-black mb-6 relative">
           <motion.div 
             className="h-full bg-accent absolute top-0 left-0"
             style={{ width: `${progress}%` }}
           />
        </div>

        <button onClick={() => { triggerHaptic('success'); onConfirm(); }} className="w-full py-4 bg-black text-white font-bold font-display uppercase border-2 border-transparent hover:bg-white hover:text-black hover:border-black hover:shadow-hard transition-all">
          I'm Awake
        </button>
      </motion.div>
    </div>
  );
};