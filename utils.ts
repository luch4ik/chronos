
export const formatTime = (timeStr: string, format: '12h' | '24h'): string => {
  if (!timeStr) return '';
  
  if (format === '24h') return timeStr;

  const [hours, minutes] = timeStr.split(':').map(Number);
  const period = hours >= 12 ? 'PM' : 'AM';
  const hours12 = hours % 12 || 12;
  
  return `${hours12.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')} ${period}`;
};

export const getTimeUntil = (timeStr: string, activeDays: number[]): string => {
  if (!timeStr) return '';
  const now = new Date();
  const [h, m] = timeStr.split(':').map(Number);
  let target = new Date(now);
  target.setHours(h, m, 0, 0);

  // Helper to add days
  const addDays = (date: Date, days: number) => {
      const result = new Date(date);
      result.setDate(result.getDate() + days);
      return result;
  };

  if (activeDays.length === 0) {
      // One-time alarm
      if (target <= now) {
          target = addDays(target, 1);
      }
  } else {
      // Recurring
      const currentDay = now.getDay();
      
      // Check if today is a valid day AND the time is in the future
      if (activeDays.includes(currentDay) && target > now) {
          // target is correct (today)
      } else {
          // Find the next day in the list
          const sortedDays = [...activeDays].sort((a, b) => a - b);
          
          // Find next day in the week
          let nextDay = sortedDays.find(d => d > currentDay);
          
          // If no later day in the week, wrap around to first day
          if (nextDay === undefined) {
              nextDay = sortedDays[0];
          }

          // Calculate days to add
          let daysDiff = nextDay - currentDay;
          if (daysDiff <= 0) daysDiff += 7; // Wrap around week
          
          target = addDays(target, daysDiff);
      }
  }

  const diffMs = target.getTime() - now.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  
  if (diffMins < 1) return "Due now";

  const hours = Math.floor(diffMins / 60);
  const minutes = diffMins % 60;

  const parts = [];
  if (hours > 0) parts.push(`${hours}h`);
  if (minutes > 0) parts.push(`${minutes}m`);
  
  return `Alarm in ${parts.join(' ')}`;
};

export const triggerHaptic = (pattern: 'light' | 'medium' | 'heavy' | 'success' | 'error' | 'selection' = 'light') => {
  if (typeof navigator !== 'undefined' && navigator.vibrate) {
    try {
        switch (pattern) {
        case 'light': navigator.vibrate(10); break;
        case 'medium': navigator.vibrate(40); break;
        case 'heavy': navigator.vibrate(70); break;
        case 'selection': navigator.vibrate(20); break;
        case 'success': navigator.vibrate([50, 50, 100]); break;
        case 'error': navigator.vibrate([50, 100, 50, 100]); break;
        }
    } catch (e) {
        // Haptics not supported or blocked
    }
  }
};
