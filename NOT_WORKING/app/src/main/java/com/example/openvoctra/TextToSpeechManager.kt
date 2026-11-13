package com.example.openvoctra

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class TextToSpeechManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val callbacks = ConcurrentHashMap<String, () -> Unit>()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.GERMAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Selected language is not available.")
                } else {
                    isInitialized = true
                    tts?.setSpeechRate(1.0f)
                    tts?.setPitch(1.0f)
                }

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}

                    override fun onDone(utteranceId: String?) {
                        utteranceId?.let { callbacks.remove(it)?.invoke() }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        utteranceId?.let { callbacks.remove(it) }
                    }
                })
            } else {
                Log.e("TTS", "TTS-Initialisierung fehlgeschlagen.")
            }
        }
    }

    fun setlanguage(language: Locale) {
        tts?.setLanguage(language)
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isInitialized || text.isBlank()) {
            onDone?.invoke()
            return
        }
        val id = "tts_${System.nanoTime()}"
        if (onDone != null) callbacks[id] = onDone
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, id)
    }

    fun stop() {
        callbacks.clear()
        tts?.stop()
    }

    fun stopall() {
        callbacks.clear()
        tts?.stop()
        tts?.shutdown()
        isInitialized = false
    }
}