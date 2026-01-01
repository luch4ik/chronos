
import React, { useRef, useEffect, useState } from 'react';
import { triggerHaptic } from '../utils';
import { audioService } from '../services/audioService';

interface TimePickerWheelProps {
  value: string; // "HH:MM" 24h format
  onChange: (value: string) => void;
  format: '12h' | '24h';
}

interface WheelColumnProps {
  options: { label: string; value: string | number }[];
  value: string | number;
  onChange: (val: string | number) => void;
  label?: string;
}

const ITEM_HEIGHT = 48; // px

const WheelColumn: React.FC<WheelColumnProps> = ({ options, value, onChange, label }) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [isScrolling, setIsScrolling] = useState(false);
  const scrollTimeout = useRef<number | null>(null);

  useEffect(() => {
    if (containerRef.current && !isScrolling) {
      const index = options.findIndex(o => o.value == value); // loose equality for string/num
      if (index !== -1) {
        containerRef.current.scrollTop = index * ITEM_HEIGHT;
      }
    }
  }, [value, isScrolling, options]);

  const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
    setIsScrolling(true);
    
    if (scrollTimeout.current) clearTimeout(scrollTimeout.current);
    scrollTimeout.current = window.setTimeout(() => {
        setIsScrolling(false);
    }, 150);

    const scrollTop = e.currentTarget.scrollTop;
    const index = Math.round(scrollTop / ITEM_HEIGHT);
    const clampedIndex = Math.max(0, Math.min(index, options.length - 1));
    const selectedOption = options[clampedIndex];

    if (selectedOption && selectedOption.value != value) {
        onChange(selectedOption.value);
        triggerHaptic('selection');
        audioService.playClick();
    }
  };

  return (
    <div className="flex flex-col items-center">
        <div className="relative h-48 w-20 sm:w-24 border-2 border-black bg-surface mx-1 shadow-[4px_4px_0px_0px_#000]">
        
        {/* Gradient Mask for 3D effect */}
        <div 
            className="absolute inset-0 z-10 pointer-events-none"
            style={{
                background: 'linear-gradient(to bottom, var(--c-surface) 0%, transparent 20%, transparent 80%, var(--c-surface) 100%)'
            }}
        />

        <div 
            ref={containerRef}
            className="h-full overflow-y-scroll snap-y snap-mandatory scrollbar-hide no-scrollbar relative z-0"
            onScroll={handleScroll}
            style={{ scrollBehavior: 'smooth', scrollbarWidth: 'none', msOverflowStyle: 'none' }}
        >
            <div style={{ height: ITEM_HEIGHT * 1.5 }}></div> {/* Padding top */}
            {options.map((opt) => (
            <div 
                key={opt.value} 
                className={`h-[48px] flex items-center justify-center snap-center font-bold font-display transition-all duration-200
                    ${opt.value == value ? 'text-textMain text-3xl scale-110 opacity-100' : 'text-textMuted text-xl scale-90 opacity-40'}
                `}
            >
                {opt.label}
            </div>
            ))}
            <div style={{ height: ITEM_HEIGHT * 1.5 }}></div> {/* Padding bottom */}
        </div>
        
        {/* Highlight Box Overlay */}
        <div className="absolute top-1/2 left-0 right-0 -translate-y-1/2 h-[48px] border-y-2 border-black pointer-events-none bg-accent/5 mix-blend-multiply dark:mix-blend-overlay z-20"></div>
        </div>
        {label && <div className="mt-2 text-[10px] font-bold font-display uppercase text-textMuted tracking-widest">{label}</div>}
    </div>
  );
};

export const TimePickerWheel: React.FC<TimePickerWheelProps> = ({ value, onChange, format }) => {
  // Parse initial 24h value
  const [hoursStr, minutesStr] = value ? value.split(':') : ['07', '00'];
  const hours24 = parseInt(hoursStr, 10);
  const minutes = parseInt(minutesStr, 10);

  const hoursOptions24 = Array.from({ length: 24 }, (_, i) => ({ 
      label: i.toString().padStart(2, '0'), 
      value: i 
  }));

  const hoursOptions12 = Array.from({ length: 12 }, (_, i) => ({
      label: (i === 0 ? 12 : i).toString().padStart(2, '0'),
      value: i === 0 ? 12 : i // 12, 1, 2, ... 11
  }));

  const minuteOptions = Array.from({ length: 60 }, (_, i) => ({
      label: i.toString().padStart(2, '0'),
      value: i
  }));

  const periodOptions = [
      { label: 'AM', value: 'AM' },
      { label: 'PM', value: 'PM' }
  ];

  // Derived state for 12h display
  const isPM = hours24 >= 12;
  const displayHour12 = hours24 % 12 || 12;

  const updateTime = (h: number, m: number) => {
      const hStr = h.toString().padStart(2, '0');
      const mStr = m.toString().padStart(2, '0');
      onChange(`${hStr}:${mStr}`);
  };

  const handleHourChange = (val: string | number) => {
      const newH = typeof val === 'number' ? val : parseInt(val, 10);
      if (format === '24h') {
          updateTime(newH, minutes);
      } else {
          // 12h logic
          // val is 1-12. isPM is current period.
          let converted = newH;
          if (converted === 12) converted = 0; // treat 12 as 0 for math
          if (isPM) converted += 12;
          updateTime(converted, minutes);
      }
  };

  const handleMinuteChange = (val: string | number) => {
      updateTime(hours24, typeof val === 'number' ? val : parseInt(val, 10));
  };

  const handlePeriodChange = (val: string | number) => {
      const newPeriod = val as string;
      if (format === '24h') return;
      
      let h = hours24;
      if (newPeriod === 'AM' && h >= 12) h -= 12;
      if (newPeriod === 'PM' && h < 12) h += 12;
      updateTime(h, minutes);
  };

  return (
    <div className="flex justify-center items-start gap-1 py-4 bg-bg select-none">
      {format === '24h' ? (
        <>
            <WheelColumn options={hoursOptions24} value={hours24} onChange={handleHourChange} label="HOURS" />
            <span className="text-2xl font-black font-display text-textMain pt-[70px]">:</span>
            <WheelColumn options={minuteOptions} value={minutes} onChange={handleMinuteChange} label="MINS" />
        </>
      ) : (
        <>
            <WheelColumn options={hoursOptions12} value={displayHour12} onChange={handleHourChange} label="HOURS" />
            <span className="text-2xl font-black font-display text-textMain pt-[70px]">:</span>
            <WheelColumn options={minuteOptions} value={minutes} onChange={handleMinuteChange} label="MINS" />
            <WheelColumn options={periodOptions} value={isPM ? 'PM' : 'AM'} onChange={handlePeriodChange} label="PERIOD" />
        </>
      )}
    </div>
  );
};
