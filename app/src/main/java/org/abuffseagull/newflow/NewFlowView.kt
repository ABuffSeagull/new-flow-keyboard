package org.abuffseagull.newflow

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.view.View
import kotlin.properties.Delegates

/**
 * This is the View class for the keyboard, which shows everything on screen
 * Created by abuffseagull on 3/28/18.
 */

const val PADDING = 5

class NewFlowView(context: Context) : View(context) {
	private val background = ShapeDrawable(RectShape())
	private val circleList = Array(5, { ShapeDrawable(OvalShape()) })
	private var keyboardHeight = 0
	private var circleSize: Int by Delegates.observable(0) { _, _, newValue ->
		keyboardHeight = newValue * 5
	}

	init {
		background.paint.color = Color.RED
		for (circle in circleList) {
			circle.paint.color = Color.BLUE
		}
	}

	private fun getCircleBounds(x: Int, y: Int): Rect {
		return Rect(
				x * circleSize + PADDING,
				y * circleSize + PADDING,
				(x + 1) * circleSize - PADDING,
				(y + 1) * circleSize - PADDING
		)
	}

	/**
	 * This is called during layout when the size of this view has changed.
	 * width and height SHOULD be the size of the entire screen, but not completely sure
	 */
	override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
		circleList[0].bounds = getCircleBounds(3, 1)
		circleList[1].bounds = getCircleBounds(2, 2)
		circleList[2].bounds = getCircleBounds(3, 2)
		circleList[3].bounds = getCircleBounds(4, 2)
		circleList[4].bounds = getCircleBounds(4, 3)
		background.setBounds(0, 0, width, height)
	}

	/**
	 *
	 */
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val screenWidth = Resources.getSystem().displayMetrics.widthPixels
		circleSize = screenWidth / 7
		setMeasuredDimension(Resources.getSystem().displayMetrics.widthPixels, circleSize * 5)
	}

	override fun onDraw(canvas: Canvas?) {
		background.draw(canvas)
		circleList.forEach { it.draw(canvas) }
	}
}