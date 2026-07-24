package com.museerodin.companion

import com.museerodin.companion.content.AppLanguage
import com.museerodin.companion.content.AudioStop
import com.museerodin.companion.content.Citation
import com.museerodin.companion.content.ContentLinkKind
import com.museerodin.companion.content.LocalizedText
import com.museerodin.companion.narration.NarrationController
import com.museerodin.companion.narration.NarrationEngine
import com.museerodin.companion.narration.NarrationEvent
import com.museerodin.companion.narration.NarrationStateMachine
import com.museerodin.companion.user.PlaybackState
import org.junit.Assert.assertEquals
import org.junit.Test

class NarrationControllerTest {
    @Test
    fun stateMachineHandlesPlaybackEvents() {
        val machine = NarrationStateMachine()
        machine.handle(NarrationEvent.Play("stop-1", "Hello", AppLanguage.EN))
        assertEquals(PlaybackState.SPEAKING, machine.state.playbackState)
        assertEquals("stop-1", machine.state.currentStopID)

        machine.handle(NarrationEvent.Pause)
        assertEquals(PlaybackState.PAUSED, machine.state.playbackState)

        machine.handle(NarrationEvent.Resume)
        assertEquals(PlaybackState.SPEAKING, machine.state.playbackState)

        machine.handle(NarrationEvent.Stop)
        assertEquals(PlaybackState.STOPPED, machine.state.playbackState)

        machine.handle(NarrationEvent.Complete)
        assertEquals(PlaybackState.COMPLETED, machine.state.playbackState)
    }

    @Test
    fun controllerTogglesPauseResumeStopAndCompletion() {
        val engine = FakeNarrationEngine()
        val controller = NarrationController(engine)
        val stop = audioStop()

        controller.toggle(stop, AppLanguage.EN)
        assertEquals(PlaybackState.SPEAKING, controller.state.value.playbackState)
        assertEquals("Read this aloud.", engine.lastText)

        controller.toggle(stop, AppLanguage.EN)
        assertEquals(PlaybackState.PAUSED, controller.state.value.playbackState)
        assertEquals(1, engine.stopCount)

        controller.toggle(stop, AppLanguage.EN)
        assertEquals(PlaybackState.SPEAKING, controller.state.value.playbackState)
        assertEquals(2, engine.speakCount)

        engine.complete(stop.id)
        assertEquals(PlaybackState.COMPLETED, controller.state.value.playbackState)

        controller.stop()
        assertEquals(PlaybackState.STOPPED, controller.state.value.playbackState)
    }

    private fun audioStop(): AudioStop = AudioStop(
        id = "stop-test",
        title = LocalizedText("Test stop", "Arret test", "Parada de prueba"),
        subtitle = LocalizedText("Subtitle", "Sous-titre", "Subtitulo"),
        linkedKind = ContentLinkKind.WORK,
        linkedID = "work-test",
        routeIDs = listOf("route-test"),
        order = 1,
        script = LocalizedText("Read this aloud.", "Lisez ceci.", "Lee esto."),
        durationSecondsEstimate = 10,
        citations = listOf(Citation(id = "cite-test", sourceID = "S01", label = "S01")),
        tags = listOf("test"),
    )
}

private class FakeNarrationEngine : NarrationEngine {
    override var onDone: ((String) -> Unit)? = null
    var lastText: String? = null
    var speakCount = 0
    var stopCount = 0

    override fun speak(text: String, languageTag: String, rate: Float, utteranceID: String) {
        lastText = text
        speakCount += 1
    }

    override fun stop() {
        stopCount += 1
    }

    override fun shutdown() = Unit

    fun complete(utteranceID: String) {
        onDone?.invoke(utteranceID)
    }
}

