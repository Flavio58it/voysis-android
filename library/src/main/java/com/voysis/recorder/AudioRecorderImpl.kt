package com.voysis.recorder

import android.content.Context
import android.media.AudioFormat
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.media.AudioFormat.ENCODING_PCM_8BIT
import android.media.AudioFormat.ENCODING_PCM_FLOAT
import android.media.AudioRecord
import android.media.AudioRecord.STATE_UNINITIALIZED
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.voysis.api.Config
import com.voysis.calculateMaxRecordingLength
import com.voysis.generateAudioRecordParams
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class AudioRecorderImpl(
        context: Context,
        config: Config,
        private val player: AudioPlayer = AudioPlayer(context),
        private var record: AudioRecord? = null,
        private val executor: Executor = Executors.newSingleThreadExecutor()) : AudioRecorder {
    private val recordParams = generateAudioRecordParams(context, config)
    private val bitDepth = AudioFormat.ENCODING_PCM_16BIT
    private val maxBytes = calculateMaxRecordingLength(recordParams.sampleRate!!, bitDepth)
    private val inProgress = AtomicBoolean()

    companion object {
        const val DEFAULT_READ_BUFFER_SIZE = 4000
        const val DEFAULT_RECORD_BUFFER_SIZE = 16000
    }

    @Synchronized
    override fun start(callback: OnDataResponse) {
        stopRecorder()
        record = record ?: createAudioRecorder()
        inProgress.set(true)
        execute(callback)
        player.playStartAudio()
    }

    private fun execute(callback: OnDataResponse) {
        if (inProgress.get()) {
            executor.execute { write(callback) }
        } else {
            callback.onComplete()
        }
    }

    @Synchronized
    override fun stop() {
        stopRecorder()
        if (inProgress.compareAndSet(true, false)) {
            player.playStopAudio()
        }
    }

    override fun getAudioInfo(): AudioInfo {
        return AudioInfo(record?.sampleRate ?: -1, getBitsPerSecond())
    }

    private fun getBitsPerSecond(): Int {
        if (Build.VERSION.SDK_INT >= 21) {
            return when (record?.audioFormat) {
                ENCODING_PCM_FLOAT -> 32
                ENCODING_PCM_16BIT -> 16
                ENCODING_PCM_8BIT -> 8
                else -> {
                    -1
                }
            }
        } else {
            return when (record?.audioFormat) {
                ENCODING_PCM_16BIT -> 16
                ENCODING_PCM_8BIT -> 8
                else -> {
                    -1
                }
            }
        }
    }

    private fun write(callback: OnDataResponse) {
        record?.startRecording()
        callback.onRecordingStarted()
        val buf = ByteBuffer.allocate(recordParams.readBufferSize!!)
        buf.clear()
        val buffer = ByteArray(recordParams.readBufferSize)
        var bytesRead: Int
        var limit = 0
        try {
            while (isRecording() && limit < maxBytes) {
                bytesRead = record?.read(buffer, 0, buffer.size)!!
                if ((bytesRead >= 0 || buf.position() > 0)) {
                    limit += bytesRead
                    buf.put(buffer, 0, bytesRead)
                    buf.flip()
                    callback.onDataResponse(buf)
                    buf.compact()
                } else {
                    break
                }
            }
        } catch (e: Exception) {
            Log.e("complete", e.toString())
        }
        callback.onComplete()
    }

    private fun isRecording(): Boolean = record?.recordingState == AudioRecord.RECORDSTATE_RECORDING

    private fun stopRecorder() {
        if (record?.state != STATE_UNINITIALIZED) {
            record?.stop()
            record?.release()
            record = null
        }
    }

    private fun createAudioRecorder(): AudioRecord {
        return AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, recordParams.sampleRate!!, AudioFormat.CHANNEL_IN_MONO, bitDepth, recordParams.recordBufferSize!!)
    }
}