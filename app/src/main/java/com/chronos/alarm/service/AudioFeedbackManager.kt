package com.chronos.alarm.service

// Phase 4: Advanced Features - Audio Feedback Manager
// Provides audio feedback for user interactions
// Plays click sounds, success tones, and error tones

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.chronos.alarm.R

class AudioFeedbackManager(context: Context) {

    private val soundPool: SoundPool
    private val sounds = mutableMapOf<AudioFeedbackType, Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load sound effects (using system tones for now)
        // In a real implementation, you would load custom sound files from res/raw/
        try {
            // Note: These are placeholder resource IDs
            // In production, add actual sound files to res/raw/ and load them here
            // sounds[AudioFeedbackType.CLICK] = soundPool.load(context, R.raw.click_sound, 1)
            // sounds[AudioFeedbackType.SUCCESS] = soundPool.load(context, R.raw.success_sound, 1)
            // sounds[AudioFeedbackType.ERROR] = soundPool.load(context, R.raw.error_sound, 1)
            // sounds[AudioFeedbackType.CHALLENGE_COMPLETE] = soundPool.load(context, R.raw.challenge_complete, 1)
        } catch (e: Exception) {
            // Silently fail if sound resources don't exist
        }
    }

    /**
     * Play audio feedback for a specific action
     */
    fun playFeedback(type: AudioFeedbackType, volume: Float = 1.0f) {
        val soundId = sounds[type]
        if (soundId != null) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        } else {
            // Fallback: use haptic instead if sound not loaded
            playFallbackBeep(type)
        }
    }

    /**
     * Fallback beep using system beep (when sound files not available)
     * Uses frequency-based tone generation
     */
    private fun playFallbackBeep(type: AudioFeedbackType) {
        // In a real implementation, use ToneGenerator for beeps
        // For now, this is a no-op placeholder
        when (type) {
            AudioFeedbackType.CLICK -> {
                // Play short click beep (frequency: 800Hz, duration: 50ms)
            }
            AudioFeedbackType.SUCCESS -> {
                // Play success tone (frequency: 1200Hz → 1400Hz, duration: 200ms)
            }
            AudioFeedbackType.ERROR -> {
                // Play error tone (frequency: 400Hz → 200Hz, duration: 300ms)
            }
            AudioFeedbackType.CHALLENGE_COMPLETE -> {
                // Play completion chime (frequency: 1000Hz → 1500Hz → 2000Hz, duration: 500ms)
            }
        }
    }

    /**
     * Play click sound for button/interaction
     */
    fun playClick(volume: Float = 0.5f) {
        playFeedback(AudioFeedbackType.CLICK, volume)
    }

    /**
     * Play success sound for positive action
     */
    fun playSuccess(volume: Float = 0.7f) {
        playFeedback(AudioFeedbackType.SUCCESS, volume)
    }

    /**
     * Play error sound for negative action
     */
    fun playError(volume: Float = 0.7f) {
        playFeedback(AudioFeedbackType.ERROR, volume)
    }

    /**
     * Play challenge completion sound
     */
    fun playChallengeComplete(volume: Float = 0.8f) {
        playFeedback(AudioFeedbackType.CHALLENGE_COMPLETE, volume)
    }

    /**
     * Release all resources
     */
    fun release() {
        soundPool.release()
        sounds.clear()
    }
}

/**
 * Types of audio feedback
 */
enum class AudioFeedbackType {
    CLICK,              // Button click sound
    SUCCESS,            // Positive action confirmation
    ERROR,              // Error or negative action
    CHALLENGE_COMPLETE  // Challenge completion chime
}
