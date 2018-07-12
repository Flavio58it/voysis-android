package com.voysis.android

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class DynamicAnimationManager(private val width: Int, val height: Int) {

    private val lines = mutableListOf<Line>()
    private val circlePaint = Paint()
    private val linePaint = Paint()
    private val squarePaint = Paint()
    private val logoPaint = Paint()
    private val rectF = RectF()

    init {
        setPaintAttributes()
        addLinesToLineList()
    }

    fun draw(canvas: Canvas, viewState: ViewState) {
        drawBackground(canvas)
        if (viewState != ViewState.IDLE) {
            drawAnimation(canvas, viewState)
        } else {
            drawLogo(canvas)
        }
    }

    fun updateLines(value: Float = 0f) {
        if (value == 0f) {
            lines[0].startY = lines[0].originalStartPosY
            lines[0].endY = lines[0].originalEndPositionY

            lines[1].startY = lines[1].originalStartPosY
            lines[1].endY = lines[1].originalEndPositionY

            lines[2].startY = lines[2].originalStartPosY
            lines[2].endY = lines[2].originalEndPositionY
        } else {
            lines[0].startY = width * 0.5f - (value*2)
            lines[0].endY = width * 0.5f + (value*2)

            lines[1].startY = lines[1].originalStartPosY + value
            lines[1].endY = lines[1].originalEndPositionY - value

            lines[2].startY = width * 0.5f - (value*2)
            lines[2].endY = width * 0.5f + (value*2)
        }


    }

    private fun setPaintAttributes() {
        linePaint.color = Color.WHITE
        linePaint.strokeWidth = width * 0.05f
        linePaint.style = Paint.Style.STROKE;
        linePaint.strokeCap = Paint.Cap.ROUND;

        circlePaint.color = Color.TRANSPARENT
        circlePaint.strokeWidth = width * 0.05f
        circlePaint.style = Paint.Style.STROKE;
        circlePaint.strokeCap = Paint.Cap.ROUND;

        squarePaint.style = Paint.Style.FILL
        squarePaint.color = Color.BLUE
        squarePaint.isAntiAlias = true

        logoPaint.style = Paint.Style.STROKE
        logoPaint.strokeWidth = width * 0.05f
        logoPaint.color = Color.WHITE
        logoPaint.strokeCap = Paint.Cap.ROUND;
        logoPaint.isAntiAlias = true
    }

    private fun addLinesToLineList() {
        lines.add(createLine(width * 0.25f, width * 0.35f, height * 0.65f))
        lines.add(createLine(width * 0.50f, width * 0.25f, height * 0.75f))
        lines.add(createLine(width * 0.75f, width * 0.35f, height * 0.65f))
    }

    private fun hideLines() {
        linePaint.color = Color.TRANSPARENT
        circlePaint.color = Color.WHITE
    }

    private fun hideCircles() {
        circlePaint.color = Color.TRANSPARENT
        linePaint.color = Color.WHITE
    }

    private fun drawAnimation(canvas: Canvas, viewState: ViewState) {
        if (viewState != ViewState.PROCESSING) {
            hideCircles()
        } else {
            hideLines()
        }
        for (line in lines) {
            canvas.drawLine(line.startX, line.startY, line.endX, line.endY, linePaint);
            val xPos = height * 0.45f + verticalPosition(System.currentTimeMillis(), line.startX.toLong())
            canvas.drawCircle(line.startX, xPos, 5f, circlePaint);
        }

    }

    private fun drawLogo(canvas: Canvas) {
        rectF.left = width * 0.37F
        rectF.right = width * 0.63F
        rectF.top = height * 0.13F
        rectF.bottom = height * 0.53F
        canvas.drawRoundRect(rectF, width * 0.45f, width * 0.45f, logoPaint)

        val start1X = height * 0.50F
        val start1Y = height * 0.85F
        val stop1X = height * 0.50F
        val stop1Y = height * 0.65F
        canvas.drawLine(start1X, start1Y, stop1X, stop1Y, logoPaint)

        val start2X = height * 0.30F
        val start2Y = height * 0.50F
        val stop2X = height * 0.50F
        val stop2Y = height * 0.65F
        canvas.drawLine(start2X, start2Y, stop2X, stop2Y, logoPaint)

        val start3X = height * 0.70F
        val start3Y = height * 0.50F
        val stop3X = height * 0.50F
        val stop3Y = height * 0.65F
        canvas.drawLine(start3X, start3Y, stop3X, stop3Y, logoPaint)
    }

    private fun drawBackground(canvas: Canvas) {
        rectF.left = 0f
        rectF.top = 0f
        rectF.right = getSquareSize(canvas)
        rectF.bottom = getSquareSize(canvas)
        canvas.drawRoundRect(rectF, 25f, 25f, squarePaint)
    }
}

data class Line(var startX: Float,
                var endX: Float,
                var startY: Float,
                var endY: Float,
                var originalStartPosY: Float = startY,
                var originalEndPositionY: Float = endY)