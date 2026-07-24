package com.museerodin.companion.narration

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.museerodin.companion.content.AppLanguage
import com.museerodin.companion.content.AudioStop
import com.museerodin.companion.user.PlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed interface NarrationEvent {
    data class Play(val stopID: String, val text: String, val language: AppLanguage) : NarrationEvent
    data object Pause : NarrationEvent
    data object Resume : NarrationEvent
    data object Stop : NarrationEvent
    data object Complete : NarrationEvent
}

data class NarrationState(
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val currentStopID: String? = null,
    val currentText: String = "",
    val currentLanguage: AppLanguage = AppLanguage.EN,
    val rate: Float = 1.0f,
)

class NarrationStateMachine {
    var state: NarrationState = NarrationState()
        private set

    fun setRate(rate: Float): NarrationState {
        state = state.copy(rate = rate.coerceIn(0.5f, 1.6f))
        return state
    }

    fun handle(event: NarrationEvent): NarrationState {
        state = when (event) {
            is NarrationEvent.Play -> state.copy(
                playbackState = PlaybackState.SPEAKING,
                currentStopID = event.stopID,
                currentText = event.text,
                currentLanguage = event.language,
            )

            NarrationEvent.Pause -> if (state.playbackState == PlaybackState.SPEAKING) {
                state.copy(playbackState = PlaybackState.PAUSED)
            } else {
                state
            }

            NarrationEvent.Resume -> if (state.playbackState == PlaybackState.PAUSED) {
                state.copy(playbackState = PlaybackState.SPEAKING)
            } else {
                state
            }

            NarrationEvent.Stop -> state.copy(playbackState = PlaybackState.STOPPED)
            NarrationEvent.Complete -> state.copy(playbackState = PlaybackState.COMPLETED)
        }
        return state
    }
}

interface NarrationEngine {
    var onDone: ((String) -> Unit)?
    fun speak(text: String, languageTag: String, rate: Float, utteranceID: String)
    fun stop()
    fun shutdown()
}

class NarrationController(private val engine: NarrationEngine) {
    private val stateMachine = NarrationStateMachine()
    private val mutableState = MutableStateFlow(stateMachine.state)
    val state: StateFlow<NarrationState> = mutableState.asStateFlow()

    var rate: Float
        get() = mutableState.value.rate
        set(value) {
            mutableState.value = stateMachine.setRate(value)
        }

    init {
        engine.onDone = {
            mutableState.value = stateMachine.handle(NarrationEvent.Complete)
        }
    }

    fun toggle(stop: AudioStop, language: AppLanguage) {
        val current = mutableState.value
        when {
            current.playbackState == PlaybackState.SPEAKING && current.currentStopID == stop.id -> pause()
            current.playbackState == PlaybackState.PAUSED && current.currentStopID == stop.id -> resume()
            else -> speak(stop, language)
        }
    }

    fun speak(stop: AudioStop, language: AppLanguage) {
        val text = stop.script.value(language)
        val utteranceID = stop.id
        mutableState.value = stateMachine.handle(NarrationEvent.Play(stop.id, text, language))
        engine.speak(text, language.languageTag, mutableState.value.rate, utteranceID)
    }

    fun pause() {
        engine.stop()
        mutableState.value = stateMachine.handle(NarrationEvent.Pause)
    }

    fun resume() {
        val current = mutableState.value
        if (current.currentStopID == null || current.currentText.isBlank()) return
        mutableState.value = stateMachine.handle(NarrationEvent.Resume)
        engine.speak(current.currentText, current.currentLanguage.languageTag, current.rate, current.currentStopID)
    }

    fun stop() {
        engine.stop()
        mutableState.value = stateMachine.handle(NarrationEvent.Stop)
    }

    fun shutdown() {
        engine.shutdown()
    }
}

class AndroidTextToSpeechEngine(context: Context) : NarrationEngine, TextToSpeech.OnInitListener {
    override var onDone: ((String) -> Unit)? = null

    private var ready = false
    private var pendingAction: (() -> Unit)? = null
    private val tts = TextToSpeech(context.applicationContext, this)

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        tts.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onError(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) {
                    utteranceId?.let { onDone?.invoke(it) }
                }
            },
        )
        if (ready) {
            pendingAction?.invoke()
            pendingAction = null
        }
    }

    override fun speak(text: String, languageTag: String, rate: Float, utteranceID: String) {
        val action: () -> Unit = {
            tts.language = Locale.forLanguageTag(languageTag)
            tts.setSpeechRate(rate)
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceID)
            Unit
        }
        if (ready) {
            action()
        } else {
            pendingAction = action
        }
    }

    override fun stop() {
        tts.stop()
    }

    override fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}

class NoOpNarrationEngine : NarrationEngine {
    override var onDone: ((String) -> Unit)? = null

    override fun speak(text: String, languageTag: String, rate: Float, utteranceID: String) = Unit
    override fun stop() = Unit
    override fun shutdown() = Unit
}
