import React from 'react';

export const Background: React.FC = () => {
  return (
    <div className="fixed inset-0 z-0 bg-bg transition-colors duration-300 pointer-events-none opacity-40">
       <div 
         className="absolute inset-0"
         style={{
           backgroundImage: `radial-gradient(var(--c-text-muted) 1px, transparent 1px)`,
           backgroundSize: '24px 24px'
         }}
       />
    </div>
  );
};