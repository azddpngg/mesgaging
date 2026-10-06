package com.example.mesgaging.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.R
import com.example.mesgaging.model.CallSoundboardEffect
import com.example.mesgaging.model.MessageEffect
import com.example.mesgaging.model.PixelAudioEmoji
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class EffectSoundManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var toneGenerator: ToneGenerator? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Loaded SoundPool sound IDs
    private val soundMap = mutableMapOf<Int, Int>()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
        } catch (e: Exception) {
            Log.e("EffectSoundManager", "Error initializing audio tools", e)
        }

        // Preload real sound effect assets from res/raw
        loadRawSound(R.raw.sfx_explosion)
        loadRawSound(R.raw.sfx_fire)
        loadRawSound(R.raw.sfx_ice)
        loadRawSound(R.raw.sfx_zap)
        loadRawSound(R.raw.sfx_bass)
        loadRawSound(R.raw.sfx_whoopee)
        loadRawSound(R.raw.sfx_applause)
        loadRawSound(R.raw.sfx_laugh)
        loadRawSound(R.raw.sfx_sad)
        loadRawSound(R.raw.sfx_fanfare)
        loadRawSound(R.raw.sfx_rimshot)
        loadRawSound(R.raw.sfx_pop)
        loadRawSound(R.raw.sfx_crash)
        loadRawSound(R.raw.sfx_boing)
        loadRawSound(R.raw.sfx_clang)
    }

    private fun loadRawSound(resId: Int) {
        try {
            val id = soundPool.load(context, resId, 1)
            soundMap[resId] = id
        } catch (e: Exception) {
            Log.e("EffectSoundManager", "Error loading sound $resId", e)
        }
    }

    fun playRawSound(resId: Int, volume: Float = 1.0f, rate: Float = 1.0f) {
        val soundId = soundMap[resId]
        if (soundId != null && soundId != 0) {
            soundPool.play(soundId, volume, volume, 1, 0, rate)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(Locale.US)
                isTtsReady = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
            }
        }
    }

    fun speak(text: String, pitch: Float = 1.0f, speechRate: Float = 1.0f) {
        if (!isTtsReady || tts == null) return
        try {
            tts?.setPitch(pitch)
            tts?.setSpeechRate(speechRate)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e("EffectSoundManager", "TTS error", e)
        }
    }

    fun stopTts() {
        tts?.stop()
    }

    fun stopCallAudio() {
        stopTts()
    }

    fun playTone(toneType: Int, durationMs: Int = 150) {
        try {
            toneGenerator?.startTone(toneType, durationMs)
        } catch (e: Exception) {
            Log.e("EffectSoundManager", "Tone error", e)
        }
    }

    // New Incoming Message audio notification
    fun playIncomingMessageSound() {
        playRawSound(R.raw.sfx_pop, volume = 0.9f)
        triggerVibration(40)
    }

    fun playQrScanSuccessSound() {
        playRawSound(R.raw.sfx_pop, volume = 1.0f, rate = 1.2f)
        triggerVibrationPattern(longArrayOf(0, 40, 60, 90), intArrayOf(0, 180, 0, 255))
    }

    // Call state sounds
    fun playCallRingTone() {
        playTone(ToneGenerator.TONE_SUP_RINGTONE, 800)
    }

    fun playCallConnectedSound() {
        playTone(ToneGenerator.TONE_PROP_PROMPT, 150)
        triggerVibration(100)
    }

    fun playCallEndSound() {
        playTone(ToneGenerator.TONE_PROP_NACK, 250)
        triggerVibration(80)
    }

    // In-Call Reaction Soundboard: Real sound effects from internet
    fun playCallSoundboardEffect(effect: CallSoundboardEffect) {
        when (effect) {
            CallSoundboardEffect.POOP -> {
                playRawSound(R.raw.sfx_whoopee, volume = 1.0f)
                triggerVibrationPattern(longArrayOf(0, 80, 40, 160), intArrayOf(0, 220, 0, 255))
            }
            CallSoundboardEffect.APPLAUSE -> {
                playRawSound(R.raw.sfx_applause, volume = 1.0f)
                triggerVibrationPattern(longArrayOf(0, 50, 40, 50, 40, 50), intArrayOf(0, 180, 0, 200, 0, 220))
            }
            CallSoundboardEffect.LAUGH -> {
                playRawSound(R.raw.sfx_laugh, volume = 1.0f)
                triggerVibrationPattern(longArrayOf(0, 40, 30, 40, 30, 50), intArrayOf(0, 190, 0, 210, 0, 230))
            }
            CallSoundboardEffect.SAD -> {
                playRawSound(R.raw.sfx_sad, volume = 1.0f)
                triggerVibration(250)
            }
            CallSoundboardEffect.PARTY -> {
                playRawSound(R.raw.sfx_fanfare, volume = 1.0f)
                triggerVibrationPattern(longArrayOf(0, 60, 30, 80, 30, 120), intArrayOf(0, 180, 0, 220, 0, 255))
            }
            CallSoundboardEffect.DRUM -> {
                playRawSound(R.raw.sfx_rimshot, volume = 1.0f)
                triggerVibrationPattern(longArrayOf(0, 50, 40, 50, 40, 100), intArrayOf(0, 200, 0, 220, 0, 255))
            }
        }
    }

    fun playPixelAudioEmoji(emoji: PixelAudioEmoji) = playCallSoundboardEffect(emoji)

    // Message Effects: Real crisp sound effects from internet & synthesis
    fun playMessageEffects(effects: Set<MessageEffect>, text: String) {
        if (effects.contains(MessageEffect.POOP)) {
            playRawSound(R.raw.sfx_whoopee, volume = 1.0f)
            triggerVibrationPattern(longArrayOf(0, 80, 40, 160), intArrayOf(0, 220, 0, 255))
        } else if (effects.contains(MessageEffect.BASS) || effects.contains(MessageEffect.BASS_BOOST) || effects.contains(MessageEffect.EAR_RAPE)) {
            playRawSound(R.raw.sfx_bass, volume = 1.0f)
            triggerVibrationPattern(longArrayOf(0, 100, 40, 200), intArrayOf(0, 240, 0, 255))
        } else if (effects.contains(MessageEffect.SHAKE) || effects.contains(MessageEffect.EXPLOSION) || effects.contains(MessageEffect.EARTHQUAKE) || effects.contains(MessageEffect.NUKE)) {
            playRawSound(R.raw.sfx_explosion, volume = 1.0f)
            triggerVibrationPattern(longArrayOf(0, 60, 30, 100, 20, 80), intArrayOf(0, 255, 0, 220, 0, 180))
        } else if (effects.contains(MessageEffect.SPARK) || effects.contains(MessageEffect.GLITCH) || effects.contains(MessageEffect.DISCO)) {
            playRawSound(R.raw.sfx_zap, volume = 0.95f)
            triggerVibration(70)
        } else if (effects.contains(MessageEffect.FIRE) || effects.contains(MessageEffect.INFERNO) || effects.contains(MessageEffect.BLOOD_MOON)) {
            playRawSound(R.raw.sfx_fire, volume = 0.95f)
            triggerVibration(60)
        } else if (effects.contains(MessageEffect.FREEZE) || effects.contains(MessageEffect.FROST) || effects.contains(MessageEffect.GHOST)) {
            playRawSound(R.raw.sfx_ice, volume = 0.95f)
            triggerVibration(50)
        } else if (effects.contains(MessageEffect.LOUD) || effects.contains(MessageEffect.MEGA_CHONK) || effects.contains(MessageEffect.RED_ALERT)) {
            playRawSound(R.raw.sfx_fanfare, volume = 1.0f)
            triggerVibration(120)
        }

        if (effects.contains(MessageEffect.TTS)) {
            val pitch = if (effects.contains(MessageEffect.BASS)) 0.6f else 1.0f
            val rate = if (effects.contains(MessageEffect.LOUD)) 1.2f else 1.0f
            speak(text, pitch = pitch, speechRate = rate)
        }
    }

    private fun triggerVibration(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }

    private fun triggerVibrationPattern(timings: LongArray, amplitudes: IntArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.vibrate(CombinedVibration.createParallel(effect))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    vibrator?.vibrate(effect)
                }
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        toneGenerator?.release()
        toneGenerator = null
        soundPool.release()
    }
}
