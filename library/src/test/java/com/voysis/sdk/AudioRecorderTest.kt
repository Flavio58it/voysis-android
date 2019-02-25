package com.voysis.sdk

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.RECORDSTATE_RECORDING
import android.media.AudioRecord.RECORDSTATE_STOPPED
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.voysis.calculateMaxRecordingLength
import com.voysis.recorder.AudioPlayer
import com.voysis.recorder.AudioRecordParams
import com.voysis.recorder.AudioRecorder
import com.voysis.recorder.AudioRecorderImpl
import com.voysis.recorder.OnDataResponse
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.ExecutorService

@RunWith(MockitoJUnitRunner::class)
class AudioRecorderTest : ClientTest() {

    @Mock
    private lateinit var executorService: ExecutorService
    @Mock
    private lateinit var player: AudioPlayer
    @Mock
    private lateinit var onDataResposne: OnDataResponse
    @Mock
    private lateinit var context: Context
    private lateinit var audioRecorder: AudioRecorder
    private var record = mock<AudioRecord> {
        on { recordingState } doReturn RECORDSTATE_RECORDING doReturn RECORDSTATE_STOPPED
        on { audioFormat } doReturn AudioFormat.ENCODING_PCM_16BIT
        on { sampleRate } doReturn 16000
    }

    @Before
    fun setup() {
        audioRecorder = spy(AudioRecorderImpl(context, config, player, AudioRecordParams(1600, 4096, 16000), record, executorService))
    }

    @Test
    fun testRecordingStart() {
        audioRecorder.start(onDataResposne)
        verify(player).playStartAudio()
        verify(executorService).execute(any())
    }

    @Test
    fun testRecordingStop() {
        audioRecorder.start(onDataResposne)
        audioRecorder.stop()
        verify(player).playStopAudio()
    }

    @Test
    fun testReadLoop() {
        doAnswer { invocation ->
            (invocation.getArgument<Any>(0) as Runnable).run()
            null
        }.whenever(executorService).execute(ArgumentMatchers.any(Runnable::class.java))
        audioRecorder.start(onDataResposne)
        verify(onDataResposne).onDataResponse(any())
        verify(onDataResposne).onComplete()
    }

    @Test
    fun testGetMimeType() {
        val audioInfoA = audioRecorder.getMimeType()
        assertEquals(audioInfoA.bitsPerSample, 16)
        assertEquals(audioInfoA.sampleRate, 16000)
    }

    @Test
    fun testMaxRecordingLength() {
        assertEquals(320000, calculateMaxRecordingLength(16000))
        assertEquals(820000, calculateMaxRecordingLength(41000))
        assertEquals(960000, calculateMaxRecordingLength(48000))
    }
}
