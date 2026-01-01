
import { AlarmAudioConfig, GeneratedSoundType, SystemSoundType } from '../types';

class AudioService {
  private audioContext: AudioContext | null = null;
  private audioPlayer: HTMLAudioElement | null = null;
  private nodes: AudioNode[] = []; // Track active nodes to stop them cleanly
  private bufferSource: AudioBufferSourceNode | null = null;

  private init() {
    if (!this.audioContext) {
      this.audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
    }
  }

  // Mobile browsers require a user interaction to 'unlock' the audio context.
  public warmup() {
    this.init();
    if (this.audioContext && this.audioContext.state === 'suspended') {
      this.audioContext.resume();
    }
  }

  public playClick() {
    this.init();
    if (!this.audioContext) return;
    if (this.audioContext.state === 'suspended') this.audioContext.resume();

    const t = this.audioContext.currentTime;
    const osc = this.audioContext.createOscillator();
    const gain = this.audioContext.createGain();

    osc.connect(gain);
    gain.connect(this.audioContext.destination);

    // Short, crisp woodblock/mechanical click
    osc.type = 'sine';
    osc.frequency.setValueAtTime(800, t);
    osc.frequency.exponentialRampToValueAtTime(400, t + 0.03);
    
    gain.gain.setValueAtTime(0.15, t);
    gain.gain.exponentialRampToValueAtTime(0.01, t + 0.03);

    osc.start(t);
    osc.stop(t + 0.04);
  }

  public playAlarm(audioConfig?: AlarmAudioConfig) {
    this.init();
    
    if (this.audioContext && this.audioContext.state === 'suspended') {
      this.audioContext.resume();
    }

    // Stop existing if any
    this.stopAlarm();

    if (!audioConfig || audioConfig.source === 'GENERATED') {
        this.playGenerated(audioConfig?.generatedType || 'CLASSIC');
    } else if (audioConfig.source === 'SYSTEM') {
        this.playSystem(audioConfig?.systemType || 'MARIMBA');
    } else if (audioConfig.source === 'URL' && audioConfig.url) {
        this.playCustomAudio(audioConfig.url);
    } else if (audioConfig.source === 'FILE' && audioConfig.fileData) {
        this.playCustomAudio(audioConfig.fileData);
    } else {
        // Fallback
        this.playGenerated('CLASSIC');
    }
  }

  private playCustomAudio(src: string) {
      this.audioPlayer = new Audio(src);
      this.audioPlayer.loop = true;
      this.audioPlayer.volume = 1.0;

      const playPromise = this.audioPlayer.play();
      
      if (playPromise !== undefined) {
          playPromise.catch(error => {
              console.warn("Custom audio playback failed, falling back to beep:", error);
              this.playGenerated('CLASSIC');
          });
      }

      this.audioPlayer.onerror = () => {
          console.warn("Custom audio error, falling back to beep.");
          this.playGenerated('CLASSIC');
      };
  }

  private playSystem(type: SystemSoundType) {
      if (!this.audioContext) return;
      const buffer = this.createRingtoneBuffer(type);
      if (!buffer) {
          this.playGenerated('CLASSIC');
          return;
      }

      this.bufferSource = this.audioContext.createBufferSource();
      this.bufferSource.buffer = buffer;
      this.bufferSource.loop = true;

      const gain = this.audioContext.createGain();
      gain.gain.value = 0.8;
      
      this.bufferSource.connect(gain);
      gain.connect(this.audioContext.destination);
      
      this.bufferSource.start();
      this.nodes.push(this.bufferSource, gain);
  }

  // Procedural generation of simple ringtones
  private createRingtoneBuffer(type: SystemSoundType): AudioBuffer | null {
      if (!this.audioContext) return null;
      const sr = this.audioContext.sampleRate;
      const duration = 2.0; // 2 second loops
      const buffer = this.audioContext.createBuffer(1, sr * duration, sr);
      const data = buffer.getChannelData(0);
      
      const t = (i: number) => i / sr;
      const note = (freq: number, time: number, dur: number, i: number, wave: 'sine' | 'saw' | 'square' = 'sine') => {
          const currentT = t(i);
          if (currentT >= time && currentT < time + dur) {
              const localT = currentT - time;
              let val = 0;
              if (wave === 'sine') val = Math.sin(2 * Math.PI * freq * localT);
              if (wave === 'square') val = Math.sign(Math.sin(2 * Math.PI * freq * localT));
              if (wave === 'saw') val = 2 * (localT * freq - Math.floor(localT * freq + 0.5));
              
              // Envelope
              const attack = 0.01;
              const decay = 0.3;
              let amp = 0;
              if (localT < attack) amp = localT / attack;
              else amp = Math.max(0, 1 - (localT - attack) / decay);
              
              return val * amp;
          }
          return 0;
      };

      for (let i = 0; i < buffer.length; i++) {
          let val = 0;
          switch (type) {
              case 'MARIMBA':
                  // Simple major arpeggio
                  // E5 (659), G#5 (830), B5 (987), E6 (1318)
                  val += note(659, 0.0, 0.4, i);
                  val += note(830, 0.2, 0.4, i);
                  val += note(987, 0.4, 0.4, i);
                  val += note(1318, 0.6, 0.4, i);
                  val += note(987, 0.8, 0.4, i);
                  val += note(659, 1.0, 0.4, i);
                  break;
              case 'COSMIC':
                   // Slide tones
                   const ct = t(i);
                   if (ct < 1.0) {
                       val += Math.sin(2 * Math.PI * (440 + ct * 440) * ct) * (1 - ct);
                   } else {
                       val += Math.sin(2 * Math.PI * (880 - (ct-1) * 440) * (ct-1)) * (1 - (ct-1));
                   }
                   val *= 0.5;
                  break;
              case 'RIPPLE':
                  // Fast sequence
                  const freqs = [523, 659, 783, 1046, 783, 659]; // C Major
                  const step = 0.15;
                  freqs.forEach((f, idx) => {
                      val += note(f, idx * step, 0.2, i, 'sine');
                  });
                  break;
              case 'CIRCUIT':
                  // 8-bit style
                   const cFreqs = [220, 0, 440, 0, 880, 660, 440, 220];
                   const cStep = 0.2;
                   cFreqs.forEach((f, idx) => {
                       if(f > 0) val += note(f, idx * cStep, 0.15, i, 'square') * 0.3;
                   });
                  break;
          }
          data[i] = val;
      }
      return buffer;
  }

  private playGenerated(type: GeneratedSoundType) {
    if (!this.audioContext) return;
    
    const now = this.audioContext.currentTime;
    const osc = this.audioContext.createOscillator();
    const gain = this.audioContext.createGain();
    
    osc.connect(gain);
    gain.connect(this.audioContext.destination);
    
    this.nodes.push(osc, gain);

    switch (type) {
        case 'DIGITAL':
            // High pitched square wave
            osc.type = 'square';
            osc.frequency.setValueAtTime(800, now);
            
            // Modulation to make it 'trill'
            const digiLfo = this.audioContext.createOscillator();
            digiLfo.type = 'square';
            digiLfo.frequency.value = 12; // Fast trill
            const digiLfoGain = this.audioContext.createGain();
            digiLfoGain.gain.value = 200;
            
            digiLfo.connect(digiLfoGain);
            digiLfoGain.connect(osc.frequency);
            digiLfo.start();
            this.nodes.push(digiLfo, digiLfoGain);
            
            gain.gain.setValueAtTime(0.05, now); // Square waves are loud
            break;

        case 'ZEN':
            // Low sine wave with slow throb
            osc.type = 'sine';
            osc.frequency.value = 200;
            
            const zenLfo = this.audioContext.createOscillator();
            zenLfo.type = 'sine';
            zenLfo.frequency.value = 0.5; // Slow Pulse
            const zenLfoGain = this.audioContext.createGain();
            zenLfoGain.gain.value = 50; 
            
            zenLfo.connect(zenLfoGain);
            zenLfoGain.connect(osc.frequency);
            zenLfo.start();
            this.nodes.push(zenLfo, zenLfoGain);
            
            gain.gain.setValueAtTime(0.6, now);
            break;

        case 'HAZARD':
            // Aggressive sawtooth
            osc.type = 'sawtooth';
            osc.frequency.value = 150;
            
            const hazLfo = this.audioContext.createOscillator();
            hazLfo.type = 'sawtooth';
            hazLfo.frequency.value = 4; // Fast alarm
            const hazLfoGain = this.audioContext.createGain();
            hazLfoGain.gain.value = 300; // Wide sweep
            
            hazLfo.connect(hazLfoGain);
            hazLfoGain.connect(osc.frequency);
            hazLfo.start();
            this.nodes.push(hazLfo, hazLfoGain);
            
            gain.gain.setValueAtTime(0.15, now);
            break;

        case 'CLASSIC':
        default:
            // Standard Triangle siren
            osc.type = 'triangle';
            osc.frequency.setValueAtTime(440, now);
            
            const classicLfo = this.audioContext.createOscillator();
            classicLfo.type = 'sine';
            classicLfo.frequency.value = 1; // 1 second cycle
            const classicGain = this.audioContext.createGain();
            classicGain.gain.value = 220; // 440 +/- 220
            
            classicLfo.connect(classicGain);
            classicGain.connect(osc.frequency);
            classicLfo.start();
            this.nodes.push(classicLfo, classicGain);
            
            gain.gain.setValueAtTime(0.5, now);
            break;
    }

    osc.start();
  }

  public stopAlarm() {
    this.nodes.forEach(node => {
        try { 
          if (node instanceof OscillatorNode) node.stop();
          if (node instanceof AudioBufferSourceNode) node.stop();
          node.disconnect(); 
        } catch(e){}
    });
    this.nodes = [];
    this.bufferSource = null;
    
    if (this.audioPlayer) {
        this.audioPlayer.pause();
        this.audioPlayer.currentTime = 0;
        this.audioPlayer.removeAttribute('src');
        this.audioPlayer = null;
    }
  }
}

export const audioService = new AudioService();
