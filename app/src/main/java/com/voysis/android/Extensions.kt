package com.voysis.android

import android.graphics.Canvas
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun verticalPosition(time: Long, offset: Long): Int {
    val p0 = 2.0 * Math.PI * (time + offset).toDouble() / 1000
    return ((((Math.sin(p0) + 1) / 2.0)*10).toInt())*2
}
fun createLine(xPos: Float, startY: Float, endY: Float): Line {
    return Line(startX = xPos, endX = xPos, startY = startY, endY = endY)
}

fun getSquareSize(canvas: Canvas): Float {
    return if (canvas.width > canvas.height) {
        canvas.height.toFloat()
    } else {
        canvas.width.toFloat()
    }
}

fun byteToShort(byteArray: ByteArray): ShortArray {
    val shortOut = ShortArray(byteArray.size / 2)
    val byteBuffer = ByteBuffer.wrap(byteArray)
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortOut)
    return shortOut
}

fun calculateVolume(buffer: ByteBuffer): Int {
    val short = byteToShort(buffer.array())
    var sumLevel = 0.0
    val bufferReadResult = short.size
    for (i in 0 until bufferReadResult) {
        sumLevel += short[i]
    }
    return (Math.abs(sumLevel / bufferReadResult)).toInt()
}
