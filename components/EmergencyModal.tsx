import React, { useEffect } from 'react';
import { motion } from 'framer-motion';
import { Siren } from 'lucide-react';
import { EmergencyContactConfig } from '../types';
import { triggerHaptic } from '../utils';

interface EmergencyModalProps {
  config: EmergencyContactConfig;
  onDismiss: () => void;
}

export const EmergencyModal: React.FC<EmergencyModalProps> = ({ config, onDismiss }) => {
  useEffect(() => {
    const timer = setTimeout(() => {
      const separator = navigator.userAgent.match(/iPhone|iPad|iPod/i) ? '&' : '?';
      if (config.method === 'CALL') window.location.href = `tel:${config.contactNumber}`;
      else window.location.href = `sms:${config.contactNumber}${separator}body=${encodeURIComponent(config.message || "Help")}`;
    }, 2000);
    return () => clearTimeout(timer);
  }, [config]);

  return (
    <div className="fixed inset-0 z-[70] flex items-center justify-center p-4 bg-red-500/50 backdrop-blur-md">
      <motion.div 
        initial={{ scale: 0.9, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        className="bg-surface border-4 border-black shadow-hard w-full max-w-md p-8 text-center"
      >
        <div className="w-20 h-20 bg-red-500 border-4 border-black text-white rounded-full flex items-center justify-center mx-auto mb-6 animate-pulse">
            <Siren size={40} />
        </div>
        
        <h2 className="text-3xl font-black font-display text-textMain mb-2 uppercase">Emergency</h2>
        <p className="font-bold text-red-600 mb-6 uppercase">Protocol Initiated</p>
        
        <div className="bg-bg p-4 mb-8 text-left border-2 border-black shadow-hard-sm">
            <div className="text-xs font-black text-textMuted uppercase tracking-wider mb-1">Contacting</div>
            <div className="text-xl font-bold font-display text-textMain uppercase">{config.contactName}</div>
            <div className="text-lg font-bold text-textMain font-mono">{config.contactNumber}</div>
        </div>

        <button onClick={() => { triggerHaptic('heavy'); onDismiss(); }} className="w-full py-4 bg-black text-white font-black font-display uppercase hover:bg-textMuted transition-colors">
          Cancel Protocol
        </button>
      </motion.div>
    </div>
  );
};