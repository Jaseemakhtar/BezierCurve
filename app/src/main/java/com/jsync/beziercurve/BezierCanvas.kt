package com.jsync.beziercurve

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.sqrt

class BezierCanvas @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    private val CONTROLPOINT_RADIUS = 8f.DptoPx()
    private val LERPPOINT_RADIUS = 6f.DptoPx()
    private val CURVEPOINT_RADIUS = 2f.DptoPx()
    private val CONTROLPOINT_DIAMETER = 16f.DptoPx()

    private val controlPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = Color.WHITE
    }

    private val controlLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = 1f.DptoPx()
        it.color = Color.WHITE
        it.strokeCap = Paint.Cap.ROUND
    }

    private val subPointRed = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = ContextCompat.getColor(context, R.color.red)
    }

    private val subPointLineRed = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.color = ContextCompat.getColor(context, R.color.red)
    }

    private val subPointBlue = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = ContextCompat.getColor(context, R.color.blue)
    }

    private val subPointLineBlue = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.color = ContextCompat.getColor(context, R.color.blue)
    }

    private val subPointYellow = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = ContextCompat.getColor(context, R.color.yellow)
    }

    private val subPointLineYellow = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.color = ContextCompat.getColor(context, R.color.yellow)
    }

    private val subPointPurple = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = ContextCompat.getColor(context, R.color.purple)
    }

    private val subPointLinePurple = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.color = ContextCompat.getColor(context, R.color.purple)
    }

    private val subPointOrange = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = ContextCompat.getColor(context, R.color.orange)
    }

    private val subPointLineOrange = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
        it.color = ContextCompat.getColor(context, R.color.orange)
    }

    private val curveBluePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.FILL
        it.color = ContextCompat.getColor(context, R.color.green)
    }

    private val pointPaints =
        listOf(subPointRed, subPointBlue, subPointOrange, subPointYellow, subPointPurple)
    private val linePaints =
        listOf(
            subPointLineRed,
            subPointLineBlue,
            subPointLineOrange,
            subPointLineYellow,
            subPointLinePurple
        )

    private val controlPoints: List<ControlPoint> = listOf(
        elements = Array(6) { ControlPoint() }
    )
    private var directSubPoints: MutableList<ControlPoint> = mutableListOf()
    private val curvePoints: MutableList<ControlPoint> = mutableListOf()


    private var lerpPointsAnimator: ValueAnimator? = null

    private var actionListener: ActionListener? = null

    private var currentColorIndex = 0
    var controlPointCount: Int = 2
        private set
    private var selectedPoint = -1
    var showLerp: Boolean = true

    var duration: Long = 2000L

    init {
        setBackgroundColor(Color.parseColor("#0E1A25"))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isAnimating()) {
            return false
        }

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                curvePoints.clear()
                for (i in 0 until controlPointCount) {
                    if (
                        controlPoints[i]
                            .getDistance(ControlPoint(event.x, event.y)) <= CONTROLPOINT_DIAMETER
                    ) {
                        selectedPoint = i
                        break
                    }
                }

            }

            MotionEvent.ACTION_MOVE -> {
                if (selectedPoint != -1) {
                    controlPoints[selectedPoint].x = event.x
                    controlPoints[selectedPoint].y = event.y
                }
            }

            MotionEvent.ACTION_UP -> {
                selectedPoint = -1
            }
            else -> return false
        }

        invalidate()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initControlPoints()
    }

    override fun onDetachedFromWindow() {
        cancelAnimation()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas?) {

        // draws the animating sub/lerp points
        if (showLerp) {
            if (lerpPointsAnimator?.isRunning == true) {
                currentColorIndex = 0
                drawSubPoints(canvas, directSubPoints)
            }
        }

        // draw white control points (control polygon)
        for (i in 0 until controlPointCount) {

            canvas?.drawCircle(
                controlPoints[i].x,
                controlPoints[i].y,
                CONTROLPOINT_RADIUS,
                controlPointPaint
            )

            // draw line to connect two points
            val nextControlPoint = i + 1
            if (nextControlPoint < controlPointCount) {
                canvas?.drawLine(
                    controlPoints[i].x,
                    controlPoints[i].y,
                    controlPoints[nextControlPoint].x,
                    controlPoints[nextControlPoint].y,
                    controlLinePaint
                )
            }
        }

        // draws bezier curve path
        for (i in 0 until curvePoints.size) {
            canvas?.drawCircle(
                curvePoints[i].x,
                curvePoints[i].y,
                CURVEPOINT_RADIUS,
                curveBluePaint
            )
        }
    }

    private fun drawSubPoints(canvas: Canvas?, subPoints: List<ControlPoint>) {
        for (i in 0 until subPoints.size) {
            canvas?.drawCircle(
                subPoints[i].x,
                subPoints[i].y,
                LERPPOINT_RADIUS,
                pointPaints[currentColorIndex]
            )

            if (i + 1 < subPoints.size) {
                canvas?.drawLine(
                    subPoints[i].x,
                    subPoints[i].y,
                    subPoints[i + 1].x,
                    subPoints[i + 1].y,
                    linePaints[currentColorIndex]
                )
            }
        }

        currentColorIndex = (currentColorIndex + 1) % pointPaints.size

        if (subPoints.last().nextPoints.size > 0) {
            drawSubPoints(canvas, subPoints.last().nextPoints)
        }
    }

    private fun generateSubPoints(subPoints: MutableList<ControlPoint>, t: Float) {
        val newSubPoints = mutableListOf<ControlPoint>()

        for (i in 0 until subPoints.size) {
            if (i + 1 < subPoints.size) {
                val newPoint = lerp(subPoints[i], subPoints[i + 1], t)
                newSubPoints.add(newPoint)
            }
        }

        subPoints.last().nextPoints = newSubPoints

        if (newSubPoints.size > 1) {
            generateSubPoints(subPoints.last().nextPoints, t)
        }

        if (newSubPoints.size == 1) {
            curvePoints.add(newSubPoints.first())
        }
    }

    private fun initControlPoints() {
        for (i in 0 until controlPointCount) {
            if (controlPoints[i].isNotInitialized()) {
                controlPoints[i].x =
                    getRandom(CONTROLPOINT_DIAMETER, width - CONTROLPOINT_DIAMETER)
                controlPoints[i].y =
                    getRandom(CONTROLPOINT_DIAMETER, height - CONTROLPOINT_DIAMETER)
            }
        }
    }

    private fun cancelAnimation() {
        lerpPointsAnimator?.cancel()
        lerpPointsAnimator?.removeAllUpdateListeners()
        lerpPointsAnimator?.removeAllListeners()
    }

    fun add() {
        if (controlPointCount < 6) {
            controlPointCount++
            initControlPoints()
            invalidate()
        }
    }

    fun remove() {
        if (controlPointCount > 2) {
            controlPointCount--
            initControlPoints()
            invalidate()
        }
    }

    @SuppressLint("Recycle")
    fun startAnimation() {
        if (lerpPointsAnimator?.isRunning == true) {
            return
        }

        curvePoints.clear()

        lerpPointsAnimator = ValueAnimator.ofFloat(0.001f, 1.000f).also { valueAnimator ->
            valueAnimator.duration = duration
            valueAnimator.addUpdateListener { listener ->
                directSubPoints.clear()
                val t = listener.animatedValue as Float

                for (i in 0 until controlPointCount) {
                    if (i + 1 < controlPointCount) {
                        val newPoint = lerp(controlPoints[i], controlPoints[i + 1], t)
                        directSubPoints.add(newPoint)
                    }
                }

                generateSubPoints(directSubPoints, t)

                invalidate()
            }
            valueAnimator.addListener(onEnd = {
                actionListener?.onAnimationEnd()
            })
        }
        lerpPointsAnimator?.start()
    }

    fun isAnimating(): Boolean = lerpPointsAnimator?.isRunning == true

    fun stopAnimation() {
        cancelAnimation()
        invalidate()
    }

    fun setActionListener(actionListener: ActionListener) {
        this.actionListener = actionListener
    }

    open class ControlPoint(
        var x: Float = -1f,
        var y: Float = -1f,
        var nextPoints: MutableList<ControlPoint> = mutableListOf()
    ) {
        fun getDistance(controlPoint: ControlPoint): Float {
            val a = abs(x - controlPoint.x)
            val b = abs(y - controlPoint.y)

            return sqrt(a * a + b * b)
        }

        fun isNotInitialized(): Boolean = x == -1f && y == -1f
    }

    interface ActionListener {
        fun onAnimationEnd()
    }
}