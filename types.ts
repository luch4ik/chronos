
export type ChallengeType = 'MATH' | 'BURST' | 'MEMORY' | 'TYPING' | 'BLUETOOTH' | 'VELOCITY';

export interface ChallengeConfig {
  id: string;
  type: ChallengeType;
  params: {
    count?: number; // Burst clicks, Memory sequence length, Math problems count, Typing phrases count
    rounds?: number; // Memory rounds
    difficulty?: 'NORMAL' | 'HARD'; // Math, Typing
    text?: string;
    deviceName?: string; // Bluetooth
    targetSpeed?: number; // Velocity (km/h)
  };
}

export interface WakeUpCheckConfig {
  enabled: boolean;
  checkDelay: number; // minutes to wait before asking
  confirmWindow: number; // minutes user has to confirm
}

export interface EmergencyContactConfig {
  enabled: boolean;
  contactName: string;
  contactNumber: string;
  method: 'SMS' | 'CALL';
  message?: string; // Only for SMS
  triggerDelay: number; // minutes after alarm starts ringing
}

export type AudioSourceType = 'GENERATED' | 'SYSTEM' | 'URL' | 'FILE';
export type GeneratedSoundType = 'CLASSIC' | 'DIGITAL' | 'ZEN' | 'HAZARD';
export type SystemSoundType = 'MARIMBA' | 'COSMIC' | 'RIPPLE' | 'CIRCUIT';

export interface AlarmAudioConfig {
  source: AudioSourceType;
  generatedType?: GeneratedSoundType;
  systemType?: SystemSoundType;
  url?: string;
  fileData?: string; // Base64 string
  fileName?: string;
}

export interface Alarm {
  id: string;
  time: string; // HH:MM format (24h)
  label: string;
  isActive: boolean;
  days: number[]; // 0-6, where 0 is Sunday. If empty, runs once.
  challenges: ChallengeConfig[];
  wakeUpCheck?: WakeUpCheckConfig;
  emergencyContact?: EmergencyContactConfig;
  audio?: AlarmAudioConfig;
}

export interface AppSettings {
  timeFormat: '12h' | '24h';
  uninstallProtection: boolean;
  volumeOverride: boolean;
  rebootProtection: boolean;
  theme: 'light' | 'dark' | 'system';
}

export type Theme = 'aurora' | 'midnight' | 'sunset';
