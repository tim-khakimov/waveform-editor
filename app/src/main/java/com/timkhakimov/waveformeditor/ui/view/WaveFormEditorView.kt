package com.timkhakimov.waveformeditor.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.timkhakimov.waveformeditor.model.WaveItem

class WaveFormEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var waves: List<WaveItem> = listOf()
    private var moveDividerType = MoveDividerType.NOTHING
    private var leftDividerPosition = 0.2f
    private var rightDividerPosition = 0.8f

    private val wavesPaint = Paint(ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    private val dividersPaint = Paint(ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE) {
            Log.d(TAG, "onTouchEvent x = ${event.x}")
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return if (isDividerTouched(event.x)) {
                    true
                } else {
                    super.onTouchEvent(event)
                }
            }
            MotionEvent.ACTION_UP -> {
                moveDividerType = MoveDividerType.NOTHING
                return super.onTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                moveDivider(event.x)
                return true
            }
            else -> return super.onTouchEvent(event)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        canvas.drawPath(
            createWavesPath(width, height, waves),
            wavesPaint
        )
        drawLeftDivider(
            canvas,
            width,
            height
        )
        drawRightDivider(
            canvas,
            width,
            height,
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
        return pointY
    }

    private fun isDividerTouched(x: Float): Boolean {
        val width = width.toFloat()
        val relativeX = x / width
        return if (Math.abs(relativeX - leftDividerPosition) < DIVIDER_RELATIVE_TOUCH_OFFSET) {
            moveDividerType = MoveDividerType.LEFT
            true
        }  else if (Math.abs(relativeX - rightDividerPosition) < DIVIDER_RELATIVE_TOUCH_OFFSET) {
            moveDividerType = MoveDividerType.RIGHT
            true
        } else {
            false
        }
    }

    private fun drawLeftDivider(
        canvas: Canvas,
        width: Float,
        height: Float,
    ) {
        val path = Path()
        val leftX = width * leftDividerPosition
        val wedgeSize = width * DIVIDER_WEDGE_RELATIVE_SIZE
        path.moveTo(leftX, 0f)
        path.lineTo(leftX + width * DIVIDER_RELATIVE_WIDTH + wedgeSize, 0f)
        path.lineTo(leftX + width * DIVIDER_RELATIVE_WIDTH, wedgeSize)
        path.lineTo(leftX + width * DIVIDER_RELATIVE_WIDTH, height)
        path.lineTo(leftX, height)
        canvas.drawPath(path, dividersPaint)
    }

    private fun drawRightDivider(
        canvas: Canvas,
        width: Float,
        height: Float,
    ) {
        val path = Path()
        val rightX = width * rightDividerPosition
        val wedgeSize = width * DIVIDER_WEDGE_RELATIVE_SIZE
        path.moveTo(rightX, 0f)
        path.lineTo(rightX, height)
        path.lineTo(rightX - width * DIVIDER_RELATIVE_WIDTH - wedgeSize, height)
        path.lineTo(rightX - width * DIVIDER_RELATIVE_WIDTH, height - wedgeSize)
        path.lineTo(rightX - width * DIVIDER_RELATIVE_WIDTH, 0f)
        canvas.drawPath(path, dividersPaint)
    }

    fun setWaves(waves: List<WaveItem>) {
        this.waves = waves
        invalidate()
    }

    private fun moveDivider(x: Float) {
        val width = width.toFloat()
        val relativeX = x / width
        if (moveDividerType == MoveDividerType.LEFT) {
            tryMoveLeftDivider(relativeX)
        } else {
            tryMoveRightDivider(relativeX)
        }
        invalidate()
    }

    private fun tryMoveLeftDivider(relativeX: Float) {
        leftDividerPosition = when {
            relativeX < 0f -> 0f
            rightDividerPosition - relativeX < DIVIDER_RELATIVE_MIN_DISTANCE -> {
                rightDividerPosition - DIVIDER_RELATIVE_MIN_DISTANCE
            }
            else -> relativeX
        }
    }

    private fun tryMoveRightDivider(relativeX: Float) {
        rightDividerPosition = when {
            relativeX > 1f -> 1f
            relativeX - leftDividerPosition < DIVIDER_RELATIVE_MIN_DISTANCE -> {
                leftDividerPosition + DIVIDER_RELATIVE_MIN_DISTANCE
            }
            else -> relativeX
        }
    }

    enum class MoveDividerType {
        LEFT,
        RIGHT,
        NOTHING,
    }

    private companion object {
        const val TAG = "WaveFormEditorView"
        const val DIVIDER_RELATIVE_MIN_DISTANCE = 0.1f
        const val DIVIDER_RELATIVE_TOUCH_OFFSET = 0.05f
        const val DIVIDER_RELATIVE_WIDTH = 0.01f
        const val DIVIDER_WEDGE_RELATIVE_SIZE = 0.04f
    }
}