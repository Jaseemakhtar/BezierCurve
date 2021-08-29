package com.jsync.beziercurve

import android.content.res.Resources
import android.util.DisplayMetrics
import com.jsync.beziercurve.BezierCanvas.ControlPoint
import kotlin.random.Random

fun Float.DptoPx(): Float {
    val metrics = Resources.getSystem().displayMetrics
    return this * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun getRandom(min: Float, max: Float): Float = Random.nextFloat() * (max - min + 0.1f) + min

fun lerp(x1: Float, x2: Float, t: Float): Float = (x2 - x1) * t + x1

fun lerp(pointA: ControlPoint, pointB: ControlPoint, t: Float): ControlPoint {
    val x = lerp(pointA.x, pointB.x, t)
    val y = lerp(pointA.y, pointB.y, t)
    return ControlPoint(x, y)
}

fun lerp(controlPoints: List<ControlPoint>, t: Float): ControlPoint {
    return if (controlPoints.size == 2) {
        lerp(controlPoints[0], controlPoints[1], t)
    } else {
        lerp(
            controlPoints[0],
            lerp(controlPoints.subList(1, controlPoints.size), t),
            t
        )
    }
}