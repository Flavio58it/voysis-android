package com.voysis.sdk

import com.voysis.model.response.Query
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DataClassTests {

    @Test
    fun dataClassTests() {
        val audioQuery = Query()
        assertEquals(audioQuery.mimeType, "audio/pcm;bits=16;rate=16000")
    }

}