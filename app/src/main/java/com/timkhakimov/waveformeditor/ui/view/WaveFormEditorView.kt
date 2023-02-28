package com.timkhakimov.waveformeditor.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.timkhakimov.waveformeditor.model.WaveItem

class WaveFormEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var waves: List<WaveItem> = listOf()

    private val wavesPaint = Paint(ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        canvas.drawPath(
            createWavesPath(width, height, waves),
            wavesPaint
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        invalidate()
    }

    private fun createWavesPath(
        width: Float,
        height: Float,
        waves: List<WaveItem>
    ): Path {
        val path = Path()
        if (waves.size < 2) {
            return path
        }
        val waveWidth = width / (waves.size - 1)
        var currentWaveHorizontalPosition = 0f
        val firstWaveItem = waves.first()
        path.moveTo(0f, calculateVerticalPointForWavePoint(height, firstWaveItem.top))
        for (index in 1 until waves.size) {
            val waveItem = waves[index]
            currentWaveHorizontalPosition += waveWidth
            path.lineTo(currentWaveHorizontalPosition, calculateVerticalPointForWavePoint(height, waveItem.top))
        }
        val lastWaveItem = waves.last()
        path.lineTo(width, calculateVerticalPointForWavePoint(height, lastWaveItem.bottom))
        for (index in waves.size - 2 downTo 0) {
            val waveItem = waves[index]
            currentWaveHorizontalPosition -= waveWidth
            path.lineTo(currentWaveHorizontalPosition, calculateVerticalPointForWavePoint(height, waveItem.bottom))
        }
        return path
    }

    private fun calculateVerticalPointForWavePoint(height: Float, value: Double): Float {
        val temp = ((1 - value) / 2).toFloat()
        val pointY = height * temp
        Log.d(TAG, "calculateVerticalPointForWavePoint: height = $height | value = $value | pointY = $pointY")
        return pointY
    }

    fun setWaves(waves: List<WaveItem>) {
        this.waves = waves
        invalidate()
    }

    private companion object {
        const val TAG = "WaveFormEditorView"
    }
}