package com.timkhakimov.waveformeditor.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.timkhakimov.waveformeditor.R
import com.timkhakimov.waveformeditor.model.WaveItem

class WaveFormEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var waves: List<WaveItem> = listOf()
    private var moveDividerType = MoveDividerType.NOTHING
    private var leftDividerPosition = 0f
    private var rightDividerPosition = 1f

    private val path = Path()

    private val wavesPaint = Paint(ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    private val dividersPaint = Paint(ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.resources.getColor(R.color.handle_color)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
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
        drawWavesPath(
            canvas,
            width,
            height,
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

    private fun drawWavesPath(
        canvas: Canvas,
        width: Float,
        height: Float,
    ) {
        path.reset()
        if (waves.size < 2) {
            return
        }
        val waveWidth = width / (waves.size - 1)
        var currentWaveHorizontalPosition = 0f
        val firstWaveItem = waves.first()
        path.moveTo(
            0f,
            calculateVerticalPointForWavePoint(height, firstWaveItem.top)
        )
        for (index in 1 until waves.size) {
            val waveItem = waves[index]
            currentWaveHorizontalPosition += waveWidth
            path.lineTo(
                currentWaveHorizontalPosition,
                calculateVerticalPointForWavePoint(height, waveItem.top)
            )
        }
        val lastWaveItem = waves.last()
        path.lineTo(
            width,
            calculateVerticalPointForWavePoint(height, lastWaveItem.bottom)
        )
        for (index in waves.size - 2 downTo 0) {
            val waveItem = waves[index]
            currentWaveHorizontalPosition -= waveWidth
            path.lineTo(
                currentWaveHorizontalPosition,
                calculateVerticalPointForWavePoint(height, waveItem.bottom)
            )
        }
        canvas.drawPath(path, wavesPaint)
    }

    private fun calculateVerticalPointForWavePoint(height: Float, value: Double): Float {
        val temp = ((1 - value) / 2).toFloat()
        return height * temp
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
        path.reset()
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
        path.reset()
        val rightX = width * rightDividerPosition
        val wedgeSize = width * DIVIDER_WEDGE_RELATIVE_SIZE
        path.moveTo(rightX, 0f)
        path.lineTo(rightX, height)
        path.lineTo(rightX - width * DIVIDER_RELATIVE_WIDTH - wedgeSize, height)
        path.lineTo(rightX - width * DIVIDER_RELATIVE_WIDTH, height - wedgeSize)
        path.lineTo(rightX - width * DIVIDER_RELATIVE_WIDTH, 0f)
        canvas.drawPath(path, dividersPaint)
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

    fun setWaves(waves: List<WaveItem>) {
        this.waves = waves
        leftDividerPosition = 0f
        rightDividerPosition = 1f
        invalidate()
    }

    fun exportSlice(): List<WaveItem> {
        val leftWaveIndex = (waves.size * leftDividerPosition).toInt()
        val rightWaveIndex = (waves.size * rightDividerPosition).toInt()
        return waves.subList(leftWaveIndex, rightWaveIndex)
    }

    enum class MoveDividerType {
        LEFT,
        RIGHT,
        NOTHING,
    }



    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(): Parcelable? {
        val parcelable = super.onSaveInstanceState()
        val savedState = SavedState(parcelable)
        savedState.left = leftDividerPosition
        savedState.right = rightDividerPosition
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState: SavedState? = state as? SavedState
        super.onRestoreInstanceState(savedState?.superState)
        savedState?.left?.let {
            leftDividerPosition = it
        }
        savedState?.right?.let {
            rightDividerPosition = it
        }
        invalidate()
    }

    private class SavedState : BaseSavedState {
        var left: Float = 0f
        var right: Float = 0f

        internal constructor(superState: Parcelable?) : super(superState) {}
        private constructor(`in`: Parcel) : super(`in`) {
            left = `in`.readFloat()
            right = `in`.readFloat()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(left)
            out.writeFloat(right)
        }

        @JvmField
        val CREATOR: Creator<SavedState> =
            object : Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls<SavedState>(size)
                }
            }
    }

    private companion object {
        const val DIVIDER_RELATIVE_MIN_DISTANCE = 0.1f
        const val DIVIDER_RELATIVE_TOUCH_OFFSET = 0.05f
        const val DIVIDER_RELATIVE_WIDTH = 0.01f
        const val DIVIDER_WEDGE_RELATIVE_SIZE = 0.04f
    }
}
