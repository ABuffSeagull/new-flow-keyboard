package org.abuffseagull.newflow

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.util.Log
import android.view.View
import kotlin.properties.Delegates

/**
 * This is the View class for the keyboard, which shows everything on screen
 * Created by abuffseagull on 3/28/18.
 */
class NewFlowView(context: Context) : View(context) {
	private val background = ShapeDrawable(RectShape())
	//	private val circleList = Array(7 * 5, { ShapeDrawable(OvalShape()) })
	private val circle = ShapeDrawable(OvalShape())
	private var keyboardHeight = 0
	private var circleSize: Int by Delegates.observable(0) { _, _, newValue ->
		keyboardHeight = newValue * 5
	}

	init {
		Log.i(TAG, "NewFlowView created\n")
		background.paint.color = Color.RED
//		for (circle in circleList) {
//			circle.paint.color = Color.BLUE
//		}
		circle.paint.color = Color.BLUE
	}

	private fun getCircleBounds(x: Int, y: Int): Rect {
		return Rect(
				x * circleSize,
				y * circleSize,
				(x + 1) * circleSize,
				(y + 1) * circleSize
		)
	}

	/**
	 * This is called during layout when the size of this view has changed.
	 * width and height SHOULD be the size of the entire screen, but not completely sure
	 */
	override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
		Log.i(TAG, "onSizeChanged called\nwidth: $width, height: $height, oldw: $oldh, oldh: $oldh")
		val topBound = height - keyboardHeight
//		circleList[0].bounds = getCircleBounds(0, 0)
//		circleList[1].bounds = getCircleBounds(2, 2)
//		circleList[2].bounds = getCircleBounds(2, 3)
//		circleList[3].bounds = getCircleBounds(2, 4)
//		circleList[4].bounds = getCircleBounds(3, 4)
//		circleList.forEach { Log.i(TAG, "${it.bounds}") }
//		for ((index, circle) in circleList.withIndex()) {
//			circle.bounds = getCircleBounds(index % 7, index / 7)
//		}
//		circleList.forEach { Log.i(TAG, "${it.bounds}") }
		circle.bounds = getCircleBounds(3, 2)
		background.setBounds(0, 0, width, height)
	}

	/**
	 *
	 */
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val screenWidth = Resources.getSystem().displayMetrics.widthPixels
		circleSize = screenWidth / 7
		setMeasuredDimension(Resources.getSystem().displayMetrics.widthPixels, circleSize * 5)
		Log.i(TAG, "NewFlowView onMeasure called")
	}

	override fun onDraw(canvas: Canvas?) {
		background.draw(canvas)
		circle.draw(canvas)
//		circleList.forEach { it.draw(canvas) }
		Log.i(TAG, "NewFlowView onDraw called")
	}
}