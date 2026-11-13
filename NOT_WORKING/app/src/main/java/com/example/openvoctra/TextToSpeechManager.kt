package com.example.openvoctra

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TextToSpeechManager (context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.GERMAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                } else {
                    isInitialized = true
                    tts?.setSpeechRate(1.0f)
                    tts?.setPitch(1.0f)
                }
            } else {
                Log.e("TTS", "TTS-Initialisierung fehlgeschlagen.")
            }
        }
    }

    fun setlanguage (language: Locale) {
        tts?.setLanguage(language)
    }

    fun speak (text: String) {
        if (!isInitialized || text.isBlank()) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_id")
    }

    fun stop() {
        tts?.stop()
    }

    fun stopall() {
        tts?.stop()
        tts?.shutdown()
        isInitialized = false
    }
}