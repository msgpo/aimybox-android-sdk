package com.justai.aimybox.speechtotext

import androidx.annotation.RequiresPermission
import com.justai.aimybox.Aimybox
import com.justai.aimybox.core.AimyboxException
import com.justai.aimybox.core.SpeechToTextException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

/**
 * Base class for speech recognizers.
 * */
abstract class SpeechToText {

    /**
     * Recognition will be canceled if no results received within this interval.
     * */
    open val recognitionTimeoutMs = 5000L

    internal lateinit var eventChannel: SendChannel<Event>
    internal lateinit var exceptionChannel: SendChannel<AimyboxException>

    /**
     * Stop audio recording, but await for final result.
     * */
    abstract fun stopRecognition()

    /**
     * Cancel recognition entirely and abandon all results.
     * */
    abstract fun cancelRecognition()

    /**
     * Start recognition.
     * The [Result.Final] and [Result.Exception] is terminal, output channel should be closed
     * after sending any of these results.
     * */
    @RequiresPermission("android.permission.RECORD_AUDIO")
    abstract fun startRecognition(): ReceiveChannel<Result>

    abstract fun destroy()

    private fun onEvent(event: Event) {
        eventChannel.offer(event)
    }

    /**
     * Send caught [SpeechToTextException] to [Aimybox.exceptions] channel.
     * */
    protected fun onException(exception: SpeechToTextException) {
        exceptionChannel.offer(exception)
    }

    /**
     * Call this function if your recognizer can detect the start of a speech.
     *
     * @see onSpeechEnd
     * */
    protected fun onSpeechStart() = onEvent(Event.SpeechStartDetected)

    /**
     * Call this function if your recognizer can detect the end of a speech.
     *
     * @see onSpeechStart
     * */
    protected fun onSpeechEnd() = onEvent(Event.SpeechEndDetected)

    /**
     * Events occurred during recognition process.
     * */
    sealed class Event {
        object RecognitionStarted : Event()
        data class RecognitionPartialResult(val text: String?): Event()
        data class RecognitionResult(val text: String?): Event()
        object EmptyRecognitionResult: Event()
        object RecognitionCancelled: Event()
        /**
         * Happens when user starts to talk.
         *
         * *Note: not every recognizer supports this event*
         * */
        object SpeechStartDetected : Event()
        /**
         * Happens when user stops talking.
         *
         * *Note: not every recognizer supports this event*
         * */
        object SpeechEndDetected : Event()
    }

    sealed class Result {
        data class Partial(val text : String?): Result()
        data class Final(val text: String?) : Result()
        data class Exception(val exception: SpeechToTextException): Result()
    }
}