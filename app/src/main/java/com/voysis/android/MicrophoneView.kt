package com.voysis.android

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.voysis.events.Callback
import com.voysis.events.FinishedReason
import com.voysis.events.VoysisException
import com.voysis.model.response.StreamResponse
import java.nio.ByteBuffer

class MicrophoneView(context: Context?, attrs: AttributeSet?) : View(context, attrs), Callback {

    private var dynamicAnimationManager: DynamicAnimationManager? = null
    private var viewState: ViewState = ViewState.IDLE
    private var state = AnimationState.IDLE
    private var level = 0

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (dynamicAnimationManager == null) {
            dynamicAnimationManager = DynamicAnimationManager(width, height)
        }
    }

    override fun onDraw(canvas: Canvas) {
        dynamicAnimationManager?.updateLines(level.toFloat())
        dynamicAnimationManager?.draw(canvas, viewState)
        if (viewState == ViewState.PROCESSING) {
            invalidate()
        }
    }

    override fun success(response: StreamResponse) {
        reset()
    }

    override fun failure(error: VoysisException) {
        reset()
    }

    override fun audioData(buffer: ByteBuffer) {
        val temp = calculateVolume(buffer)

        if (state == AnimationState.IDLE) {
            invalidate()
            state = AnimationState.ANIMATING
        }

        if (temp > 5 && temp != level) {
            level = (temp * 0.5).toInt()
            invalidate()
        }
    }

    override fun recordingStarted() {
        viewState = ViewState.RECORDING
    }

    override fun recordingFinished(reason: FinishedReason) {
        viewState = ViewState.PROCESSING
        invalidate()
    }

    private fun reset() {
        level = 0
        viewState = ViewState.IDLE
        state = AnimationState.IDLE
        dynamicAnimationManager?.updateLines()
        invalidate()
    }
}

enum class ViewState {
    IDLE, RECORDING, PROCESSING
}

enum class AnimationState {
    IDLE, ANIMATING
}

