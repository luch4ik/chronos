import React, { useEffect, useState, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Bell, Zap, Brain, Grid3X3, Keyboard, Check, X, Bluetooth, Activity, Smartphone, Loader2, ArrowRight } from 'lucide-react';
import { Alarm, ChallengeConfig } from '../types';
import { triggerHaptic } from '../utils';

interface AlarmScreenProps {
  alarm: Alarm;
  onDismiss: () => void;
  isPreview?: boolean;
}

export const AlarmScreen: React.FC<AlarmScreenProps> = ({ alarm, onDismiss, isPreview = false }) => {
  const [currentChallengeIndex, setCurrentChallengeIndex] = useState(0);
  const [isTransitioning, setIsTransitioning] = useState(false);
  
  // --- Game States ---
  
  // Burst
  const [burstCount, setBurstCount] = useState(0);
  
  // Math
  const [mathState, setMathState] = useState({ 
      problem: { a: 0, b: 0, ans: 0 }, 
      input: '', 
      isWrong: false,
      solvedCount: 0,
      totalCount: 1 
  });
  
  // Memory
  const [memoryState, setMemoryState] = useState<{ 
      sequence: number[]; 
      playerSequence: number[]; 
      isPlaying: boolean; 
      failed: boolean;
      round: number;
      totalRounds: number;
      activePad: number | null;
  }>({ 
      sequence: [], 
      playerSequence: [], 
      isPlaying: false, 
      failed: false,
      round: 0,
      totalRounds: 1,
      activePad: null
  });
  
  // Typing
  const [typingState, setTypingState] = useState({ 
      target: '', 
      input: '', 
      isWrong: false,
      solvedCount: 0,
      totalCount: 1
  });
  
  // Bluetooth & Velocity
  const [btStatus, setBtStatus] = useState<'idle' | 'scanning' | 'connected' | 'error'>('idle');
  const [currentSpeed, setCurrentSpeed] = useState(0);
  const watchIdRef = useRef<number | null>(null);

  const currentChallenge = alarm.challenges[currentChallengeIndex];

  // --- Initialization & Lifecycle ---

  useEffect(() => {
    // Reset transient states
    setBtStatus('idle'); 
    setCurrentSpeed(0);
    if (watchIdRef.current !== null) { 
        navigator.geolocation.clearWatch(watchIdRef.current); 
        watchIdRef.current = null; 
    }

    if (!currentChallenge) {
        return;
    }

    if (currentChallenge.type === 'BURST') {
        setBurstCount(currentChallenge.params.count || 15);
    }
    else if (currentChallenge.type === 'MATH') {
        initMath(currentChallenge.params.difficulty === 'HARD', currentChallenge.params.count || 3);
    }
    else if (currentChallenge.type === 'MEMORY') {
        initMemory(currentChallenge.params.count || 5, currentChallenge.params.rounds || 3);
    }
    else if (currentChallenge.type === 'TYPING') {
        initTyping(currentChallenge.params.difficulty === 'HARD', currentChallenge.params.count || 1);
    }
    else if (currentChallenge.type === 'VELOCITY') {
        initVelocity();
    }
  }, [currentChallengeIndex, currentChallenge]);

  useEffect(() => {
      return () => {
          if (watchIdRef.current !== null) navigator.geolocation.clearWatch(watchIdRef.current);
      };
  }, []);

  const nextStep = () => {
      triggerHaptic('success');
      setIsTransitioning(true);
      setTimeout(() => {
          if (currentChallengeIndex < alarm.challenges.length - 1) {
              setCurrentChallengeIndex(prev => prev + 1);
              setIsTransitioning(false);
          } else {
              onDismiss();
          }
      }, 500);
  };

  // --- Logic: Burst ---
  const handleBurst = () => { 
      triggerHaptic('light');
      if (burstCount > 1) {
          setBurstCount(prev => prev - 1); 
      } else { 
          nextStep(); 
      }
  };

  // --- Logic: Math ---
  const generateMathProblem = (isHard: boolean) => {
      const a = isHard ? Math.floor(Math.random() * 50) + 20 : Math.floor(Math.random() * 20) + 5;
      const b = isHard ? Math.floor(Math.random() * 50) + 20 : Math.floor(Math.random() * 20) + 5;
      return { a, b, ans: a + b };
  };

  const initMath = (isHard: boolean, count: number) => {
      setMathState({ 
          problem: generateMathProblem(isHard), 
          input: '', 
          isWrong: false,
          solvedCount: 0,
          totalCount: count
      });
  };

  const appendMathInput = (val: string) => {
      triggerHaptic('light');
      if (mathState.input.length < 4) {
          setMathState(prev => ({ ...prev, input: prev.input + val }));
      }
  };

  const clearMathInput = () => {
      triggerHaptic('heavy');
      setMathState(prev => ({ ...prev, input: prev.input.slice(0, -1) }));
  };

  const submitMath = () => {
    if (parseInt(mathState.input) === mathState.problem.ans) {
        triggerHaptic('success');
        const newSolved = mathState.solvedCount + 1;
        if (newSolved >= mathState.totalCount) {
            nextStep();
        } else {
            setMathState(prev => ({
                ...prev,
                solvedCount: newSolved,
                problem: generateMathProblem(currentChallenge?.params.difficulty === 'HARD'),
                input: ''
            }));
        }
    } else { 
        triggerHaptic('error');
        setMathState(prev => ({ ...prev, isWrong: true, input: '' })); 
        setTimeout(() => setMathState(prev => ({ ...prev, isWrong: false })), 500); 
    }
  };

  // --- Logic: Memory ---
  const initMemory = (length: number, rounds: number) => {
    const startLength = length > 2 ? length - 2 : 3;
    const initialSeq = Array.from({ length: startLength }, () => Math.floor(Math.random() * 9)); 
    
    setMemoryState({ 
        sequence: initialSeq, 
        playerSequence: [], 
        isPlaying: true, 
        failed: false,
        round: 1,
        totalRounds: rounds,
        activePad: null
    });
    
    setTimeout(() => playSequence(initialSeq), 1000);
  };

  const playSequence = async (seq: number[]) => {
      setMemoryState(prev => ({ ...prev, isPlaying: true, playerSequence: [] }));
      
      for (let i = 0; i < seq.length; i++) {
          await new Promise(r => setTimeout(r, 400));
          setMemoryState(prev => ({ ...prev, activePad: seq[i] }));
          await new Promise(r => setTimeout(r, 400));
          setMemoryState(prev => ({ ...prev, activePad: null }));
      }
      
      setMemoryState(prev => ({ ...prev, isPlaying: false }));
  };

  const handleMemoryPad = (index: number) => {
      if (memoryState.isPlaying) return;

      const expected = memoryState.sequence[memoryState.playerSequence.length];
      
      if (index === expected) {
          triggerHaptic('light');
          const newPlayerSeq = [...memoryState.playerSequence, index];
          setMemoryState(prev => ({ ...prev, playerSequence: newPlayerSeq }));

          if (newPlayerSeq.length === memoryState.sequence.length) {
              if (memoryState.round >= memoryState.totalRounds) {
                  nextStep();
              } else {
                  triggerHaptic('success');
                  const nextSeq = [...memoryState.sequence, Math.floor(Math.random() * 9)];
                  setMemoryState(prev => ({ 
                      ...prev, 
                      round: prev.round + 1, 
                      sequence: nextSeq,
                      playerSequence: [],
                      isPlaying: true 
                  }));
                  setTimeout(() => playSequence(nextSeq), 1000);
              }
          }
      } else {
          triggerHaptic('error');
          setMemoryState(prev => ({ ...prev, failed: true, isPlaying: true }));
          setTimeout(() => {
              setMemoryState(prev => ({ ...prev, failed: false }));
              playSequence(memoryState.sequence); 
          }, 1000);
      }
  };

  // --- Logic: Typing ---
  const phrases = [
      "I am awake and ready",
      "The early bird catches the worm",
      "Rise and shine",
      "Carpe Diem",
      "Focus implies saying no",
      "Simplicity is the ultimate sophistication"
  ];

  const initTyping = (isHard: boolean, count: number) => {
      setTypingState({
          target: phrases[Math.floor(Math.random() * phrases.length)],
          input: '',
          isWrong: false,
          solvedCount: 0,
          totalCount: count
      });
  };

  const handleTypingSubmit = (e: React.FormEvent) => {
      e.preventDefault();
      if (typingState.input.toLowerCase().trim() === typingState.target.toLowerCase().trim()) {
          triggerHaptic('success');
          const newSolved = typingState.solvedCount + 1;
          if (newSolved >= typingState.totalCount) {
              nextStep();
          } else {
              setTypingState(prev => ({
                  ...prev,
                  solvedCount: newSolved,
                  target: phrases[Math.floor(Math.random() * phrases.length)],
                  input: ''
              }));
          }
      } else {
          triggerHaptic('error');
          setTypingState(prev => ({ ...prev, isWrong: true }));
          setTimeout(() => setTypingState(prev => ({ ...prev, isWrong: false })), 500);
      }
  };

  // --- Logic: Velocity ---
  const initVelocity = () => {
      if ('geolocation' in navigator) {
          watchIdRef.current = navigator.geolocation.watchPosition(
              (pos) => {
                  if (pos.coords.speed !== null) {
                      const kmh = pos.coords.speed * 3.6;
                      setCurrentSpeed(kmh);
                      const target = currentChallenge?.params.targetSpeed || 10;
                      if (kmh >= target) {
                          nextStep();
                      }
                  }
              },
              (err) => console.error(err),
              { enableHighAccuracy: true }
          );
      }
  };

  // --- Logic: Bluetooth ---
  const handleBluetoothScan = async () => {
      triggerHaptic('medium');
      setBtStatus('scanning');
      try {
          // @ts-ignore
          const device = await navigator.bluetooth.requestDevice({
              acceptAllDevices: true
          });
          if (device) {
              setBtStatus('connected');
              setTimeout(nextStep, 1000);
          }
      } catch (e) {
          triggerHaptic('error');
          setBtStatus('error');
          setTimeout(() => setBtStatus('idle'), 2000);
      }
  };

  // --- Renders ---

  const renderMath = () => (
      <div className="flex flex-col items-center w-full max-w-xs mx-auto">
          <div className="mb-8 p-6 bg-surface border-2 border-black shadow-hard w-full text-center">
              <span className="text-4xl font-bold font-display text-textMain">
                  {mathState.problem.a} + {mathState.problem.b} = ?
              </span>
          </div>
          
          <div className={`text-5xl font-bold font-display mb-8 h-16 tracking-widest ${mathState.isWrong ? 'text-red-500' : 'text-accent'}`}>
              {mathState.input}
              <span className="animate-pulse opacity-50">|</span>
          </div>

          <div className="grid grid-cols-3 gap-3 w-full">
              {[1, 2, 3, 4, 5, 6, 7, 8, 9].map(n => (
                  <button 
                    key={n}
                    onClick={() => appendMathInput(n.toString())}
                    className="h-16 bg-surface border-2 border-black text-2xl font-bold hover:shadow-hard-sm active:translate-y-1 active:shadow-none transition-all"
                  >
                      {n}
                  </button>
              ))}
              <button onClick={clearMathInput} className="h-16 bg-red-100 border-2 border-black text-red-500 hover:bg-red-200 active:translate-y-1 transition-all flex items-center justify-center">
                  <X size={28} />
              </button>
              <button onClick={() => appendMathInput('0')} className="h-16 bg-surface border-2 border-black text-2xl font-bold hover:shadow-hard-sm active:translate-y-1 active:shadow-none transition-all">0</button>
              <button onClick={submitMath} className="h-16 bg-accent border-2 border-black text-white flex items-center justify-center shadow-hard hover:shadow-hard-hover active:shadow-none active:translate-y-1 transition-all">
                  <Check size={32} />
              </button>
          </div>
      </div>
  );

  const renderBurst = () => (
      <div className="flex flex-col items-center justify-center h-full pb-20">
          <motion.button
              whileTap={{ scale: 0.95 }}
              onClick={handleBurst}
              className="relative w-64 h-64 bg-accent border-4 border-black flex items-center justify-center shadow-hard group outline-none overflow-hidden"
          >
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none opacity-20">
                  <Zap size={150} className="text-black rotate-12" />
              </div>
              <div className="z-10 text-center">
                  <span className="block text-8xl font-black font-display text-white tabular-nums mb-1 drop-shadow-[4px_4px_0px_#000]">{burstCount}</span>
                  <span className="text-sm font-bold bg-white text-black px-2 py-1 border-2 border-black uppercase">Taps Left</span>
              </div>
          </motion.button>
          <div className="mt-8 text-textMain font-bold font-display uppercase tracking-wider bg-surface px-4 py-2 border-2 border-black shadow-hard-sm">TAP TO WAKE</div>
      </div>
  );

  const renderMemory = () => (
      <div className="flex flex-col items-center w-full max-w-sm mx-auto">
           <div className="mb-8 text-center bg-surface border-2 border-black p-4 shadow-hard">
              <h3 className="text-xl font-bold font-display text-textMain uppercase">Memory Pattern</h3>
              <p className="text-textMuted font-medium text-sm mt-1">
                  {memoryState.failed ? <span className="text-red-500 font-bold">WRONG! WATCH!</span> : (memoryState.isPlaying ? "WATCH..." : "REPEAT")}
              </p>
           </div>
           
           <div className="grid grid-cols-3 gap-3 w-full aspect-square bg-black p-3 border-2 border-black">
                {[0, 1, 2, 3, 4, 5, 6, 7, 8].map(i => (
                    <motion.button
                        key={i}
                        whileTap={{ scale: 0.9 }}
                        onClick={() => handleMemoryPad(i)}
                        className={`
                            border-2 border-black transition-all duration-100
                            ${memoryState.activePad === i 
                                ? 'bg-accent shadow-[0_0_15px_rgba(139,92,246,0.8)] z-10 scale-105' 
                                : 'bg-surface hover:bg-surfaceHover'}
                        `}
                    />
                ))}
           </div>
           <div className="mt-8 flex gap-2">
               {Array.from({ length: memoryState.totalRounds }).map((_, i) => (
                   <div key={i} className={`h-4 w-4 border-2 border-black transition-all duration-300 ${i < memoryState.round - 1 ? 'bg-accent' : (i === memoryState.round - 1 ? 'bg-accent/50' : 'bg-surface')}`} />
               ))}
           </div>
      </div>
  );

  const renderTyping = () => (
      <div className="w-full max-w-md mx-auto flex flex-col items-center">
          <div className="mb-2 text-xs font-bold bg-black text-white px-2 py-1 uppercase tracking-widest rotate-1">Type the phrase</div>
          <div className="mb-8 p-6 bg-surface border-2 border-black shadow-hard w-full text-center">
              <p className="text-xl font-bold font-display text-textMain leading-relaxed select-none uppercase">
                  {typingState.target}
              </p>
          </div>
          
          <form onSubmit={handleTypingSubmit} className="w-full relative">
              <input 
                  autoFocus
                  type="text" 
                  value={typingState.input}
                  onChange={(e) => setTypingState(prev => ({ ...prev, input: e.target.value }))}
                  className={`
                      w-full bg-surface border-2 border-black p-4 text-lg font-bold font-display text-center outline-none transition-all shadow-hard-sm focus:shadow-hard
                      ${typingState.isWrong ? 'bg-red-100 text-red-500' : 'text-textMain'}
                  `}
                  placeholder="TYPE HERE..."
              />
              <button 
                type="submit"
                className="absolute right-3 top-3 bottom-3 aspect-square bg-accent border-2 border-black text-white flex items-center justify-center hover:bg-accentHover disabled:opacity-50"
                disabled={!typingState.input}
              >
                  <ArrowRight size={24} />
              </button>
          </form>
      </div>
  );

  const renderVelocity = () => (
      <div className="flex flex-col items-center justify-center text-center">
           <div className="border-4 border-black p-4 rounded-full mb-6 bg-surface shadow-hard">
                <Activity size={48} className="text-black" />
           </div>
           <h3 className="text-3xl font-black font-display text-textMain mb-2 uppercase italic">Run Faster!</h3>
           <p className="text-textMuted font-bold mb-8">SPEED REQUIRED: {currentChallenge?.params.targetSpeed} KM/H</p>
           
           <div className="text-8xl font-black font-display text-accent tabular-nums drop-shadow-[4px_4px_0px_#000]">
               {Math.round(currentSpeed)}
               <span className="text-2xl text-black ml-2">KM/H</span>
           </div>
      </div>
  );

  const renderBluetooth = () => (
      <div className="flex flex-col items-center justify-center text-center">
          <div className={`w-24 h-24 border-4 border-black flex items-center justify-center mb-6 transition-colors shadow-hard ${btStatus === 'connected' ? 'bg-green-400' : 'bg-surface'}`}>
             {btStatus === 'scanning' ? <Loader2 className="animate-spin" size={40} /> : <Bluetooth size={40} />}
          </div>
          
          <h3 className="text-2xl font-black font-display text-textMain mb-2 uppercase">Connect Device</h3>
          <p className="text-textMuted font-bold mb-8 max-w-xs border-2 border-black p-2 bg-white">
              TARGET: {currentChallenge?.params.deviceName || 'ANY'}
          </p>

          <button 
             onClick={handleBluetoothScan}
             disabled={btStatus === 'scanning' || btStatus === 'connected'}
             className="px-8 py-4 bg-accent border-2 border-black text-white font-bold font-display shadow-hard hover:shadow-hard-hover active:shadow-none active:translate-x-1 active:translate-y-1 transition-all disabled:opacity-50"
          >
              {btStatus === 'scanning' ? 'SEARCHING...' : (btStatus === 'connected' ? 'CONNECTED!' : 'SCAN & CONNECT')}
          </button>
      </div>
  );

  const renderCurrentChallenge = () => {
      if (!currentChallenge) {
          return (
              <div className="flex flex-col items-center">
                  <Bell className="w-20 h-20 text-accent mb-6 animate-bounce" />
                  <button onClick={() => { triggerHaptic('success'); onDismiss(); }} className="px-12 py-5 bg-accent border-2 border-black text-white font-black text-xl shadow-hard hover:shadow-hard-hover active:shadow-none active:translate-y-1 transition-all">
                      I'M AWAKE
                  </button>
              </div>
          );
      }

      switch (currentChallenge.type) {
          case 'MATH': return renderMath();
          case 'BURST': return renderBurst();
          case 'MEMORY': return renderMemory();
          case 'TYPING': return renderTyping();
          case 'VELOCITY': return renderVelocity();
          case 'BLUETOOTH': return renderBluetooth();
          default: return null;
      }
  };

  return (
    <div className="fixed inset-0 z-[100] bg-bg flex flex-col overflow-hidden">
      {/* Brutalist Header */}
      <div className="relative z-10 flex-none p-6 flex flex-col items-center border-b-2 border-black bg-surface shadow-hard-sm">
          <div className="text-sm font-bold bg-black text-white px-2 py-0.5 tracking-widest uppercase mb-2 rotate-1">
              {isPreview ? 'Preview' : 'Wake Up Protocol'}
          </div>
          <div className="text-6xl font-display font-black text-textMain tracking-tighter tabular-nums">
              {alarm.time}
          </div>
      </div>

      {/* Challenge Area */}
      <div className="relative z-10 flex-1 flex flex-col justify-center items-center p-6 w-full max-w-2xl mx-auto">
          <AnimatePresence mode="wait">
              {!isTransitioning && (
                  <motion.div
                    key={currentChallengeIndex}
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: -20 }}
                    transition={{ type: "spring", bounce: 0, duration: 0.3 }}
                    className="w-full"
                  >
                      {renderCurrentChallenge()}
                  </motion.div>
              )}
          </AnimatePresence>
      </div>

      {/* Footer / Progress */}
      <div className="relative z-10 flex-none p-8 flex justify-center gap-4 bg-bg border-t-2 border-black">
          {alarm.challenges && alarm.challenges.length > 1 && alarm.challenges.map((_, idx) => (
              <div 
                key={idx} 
                className={`
                    h-4 w-4 border-2 border-black transition-all duration-300
                    ${idx === currentChallengeIndex ? 'bg-accent rotate-45' : (idx < currentChallengeIndex ? 'bg-black' : 'bg-white')}
                `}
              />
          ))}
      </div>
    </div>
  );
};