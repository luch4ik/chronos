import React from 'react';
import { motion } from 'framer-motion';
import { AppSettings } from '../types';

interface ClockDisplayProps {
  date: Date;
  settings: AppSettings;
}

export const ClockDisplay: React.FC<ClockDisplayProps> = ({ date, settings }) => {
  const hoursRaw = date.getHours();
  const minutes = date.getMinutes().toString().padStart(2, '0');
  const day = date.toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' });

  let hoursDisplay = hoursRaw.toString().padStart(2, '0');
  let period = '';

  if (settings.timeFormat === '12h') {
    const h12 = hoursRaw % 12 || 12;
    hoursDisplay = h12.toString().padStart(2, '0');
    period = hoursRaw >= 12 ? 'PM' : 'AM';
  }

  return (
    <div className="flex flex-col items-center justify-center pt-12 pb-8 z-10 relative w-full max-w-2xl mx-auto select-none cursor-default">
      
      {/* Date Tag */}
      <motion.div 
        className="font-display font-bold text-sm bg-accent text-accentText px-3 py-1 border-2 border-black shadow-hard-sm mb-4 transform -rotate-2"
        initial={{ y: 10, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.2 }}
      >
        {day.toUpperCase()}
      </motion.div>

      {/* Main Clock */}
      <div className="relative flex items-center justify-center font-display text-textMain leading-none gap-2">
        <motion.div 
          className="text-[6rem] md:text-[9rem] font-bold tracking-tighter tabular-nums drop-shadow-[4px_4px_0px_rgba(0,0,0,1)] dark:drop-shadow-[4px_4px_0px_#000]"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.4, ease: "backOut" }}
        >
          {hoursDisplay}
        </motion.div>
        
        <motion.div 
          animate={{ opacity: [1, 1, 0, 0] }}
          transition={{ 
            duration: 1, 
            repeat: Infinity, 
            times: [0, 0.5, 0.5, 1],
            ease: "linear"
          }}
          className="text-[5rem] md:text-[8rem] font-bold pb-4 drop-shadow-[4px_4px_0px_rgba(0,0,0,1)] dark:drop-shadow-[4px_4px_0px_#000]"
        >
          :
        </motion.div>
        
        <motion.div 
          className="text-[6rem] md:text-[9rem] font-bold tracking-tighter tabular-nums drop-shadow-[4px_4px_0px_rgba(0,0,0,1)] dark:drop-shadow-[4px_4px_0px_#000]"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.4, ease: "backOut", delay: 0.1 }}
        >
          {minutes}
        </motion.div>

        {settings.timeFormat === '12h' && (
          <span className="absolute -right-8 bottom-8 text-2xl font-bold font-display border-2 border-black bg-surface px-2 shadow-hard-sm rotate-6">
            {period}
          </span>
        )}
      </div>
    </div>
  );
};