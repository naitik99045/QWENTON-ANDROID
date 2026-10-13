package ai.qwenton.apex.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechManager(
    context: Context,
    private val onInitResult: ((Boolean) -> Unit)? = null
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                isInitialized = true
                onInitResult?.invoke(true)
            } else {
                onInitResult?.invoke(false)
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized || tts == null) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "apex_utterance_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
