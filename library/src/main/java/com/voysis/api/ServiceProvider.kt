package com.voysis.api

import android.content.Context
import com.google.gson.Gson
import com.voysis.generateAudioRecordParams
import com.voysis.generateOkHttpClient
import com.voysis.getHeaders
import com.voysis.recorder.AudioRecorder
import com.voysis.recorder.AudioRecorderImpl
import com.voysis.rest.RestClient
import com.voysis.sevice.Converter
import com.voysis.sevice.ServiceImpl
import com.voysis.sevice.TokenManager
import com.voysis.websocket.WebSocketClient
import okhttp3.OkHttpClient

/**
 * The Voysis.ServiceProvider is the sdk's primary object for making Voysis.Service instances.
 */
class ServiceProvider {

    /**
     * @param config config
     * @param okClient optional okhttp client
     * @param audioRecorder optional to override default recorder
     * @return new `Service` instance
     */
    @JvmOverloads
    fun make(context: Context,
             config: Config,
             okClient: OkHttpClient? = null,
             audioRecorder: AudioRecorder = AudioRecorderImpl(generateAudioRecordParams(context, config))): Service {
        val converter = Converter(getHeaders(context), Gson())
        val client = createClient(config, generateOkHttpClient(okClient), converter)
        return ServiceImpl(client, audioRecorder, converter, config.userId, TokenManager(config.refreshToken))
    }

    /**
     * @param config config
     * @param audioRecorder optional to override default recorder
     * @return new `Service` instance
     */
    fun make(context: Context,
             config: Config,
             audioRecorder: AudioRecorder = AudioRecorderImpl(generateAudioRecordParams(context, config))): Service {
        return make(context, config, null, audioRecorder)
    }

    private fun createClient(config: Config, okClient: OkHttpClient, converter: Converter): Client {
        return if (config.isVadEnabled) {
            WebSocketClient(config, converter, okClient)
        } else {
            RestClient(config, converter, okClient)
        }
    }
}
