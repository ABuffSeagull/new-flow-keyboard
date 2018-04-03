package org.abuffseagull.newflow

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.inputmethod.InputConnection
import kotlin.math.roundToInt
import kotlin.properties.Delegates

/**
 * This is the View class for the keyboard, which shows everything on screen
 * Created by abuffseagull on 3/28/18.
 */

const val PADDING = 5

data class KeyRegion(val char: Char, val rect: Rect = Rect(), var paintIndex: Int = 0)

const val KEY_LIST = "?xwvyb<,therm .caiolp1ksnudj^z'gfq#"

class NewFlowView(context: Context) : View(context) {
	private val background = ShapeDrawable(RectShape())
	private val circleList = Array(5) { ShapeDrawable(OvalShape()) }
	private val keyRegions = Array(5 * 7) { KeyRegion(KEY_LIST[it]) }
	private var keyboardHeight = 0
	private val paintArray = Array(2) { Paint() }

	private var circleSize: Int by Delegates.observable(0) { _, _, newValue ->
		// TODO: this might be overkill
		keyboardHeight = newValue * 5
	}

	init {
		val colorArray = context.theme.obtainStyledAttributes(intArrayOf(
				android.R.attr.colorBackground,
				android.R.attr.textColorPrimary,
				android.R.attr.textColorPrimaryInverse
		))
		background.paint.color = colorArray.getColor(0, 0xFF00FF)
		paintArray[0].color = colorArray.getColor(1, 0xFF00FF) // TODO: what are these errors?
		paintArray[1].color = colorArray.getColor(2, 0xFF00FF)
		colorArray.recycle()

		circleList.forEach { it.paint.color = resources.getColor(R.color.primary) }
		for (paint in paintArray) {
			paint.textSize = 70f // TODO: This should be sized according to the screen?
			paint.textAlign = Paint.Align.CENTER
			paint.flags = Paint.ANTI_ALIAS_FLAG
		}
		keyRegions.filter { "aeiou".contains(it.char) }.forEach { it.paintIndex = 1 }
	}

	private fun getCircleBounds(x: Int, y: Int) =
			Rect(
					x * circleSize + PADDING,
					y * circleSize + PADDING,
					(x + 1) * circleSize - PADDING,
					(y + 1) * circleSize - PADDING
			)

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
		keyRegions.forEachIndexed { i, keyRegion ->
			keyRegion.rect.set(
					(i % 7) * circleSize,
					i / 7 * circleSize,
					(i % 7 + 1) * circleSize,
					(i / 7 + 1) * circleSize
			)
		}

	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val screenWidth = Resources.getSystem().displayMetrics.widthPixels
		circleSize = screenWidth / 7
		setMeasuredDimension(Resources.getSystem().displayMetrics.widthPixels, circleSize * 5)
	}


	override fun onDraw(canvas: Canvas?) {
		if (canvas == null) return
		background.draw(canvas)
		circleList.forEach { it.draw(canvas) }
		keyRegions.forEach {
			canvas.drawText( // TODO: probably also change this to StaticLayout
					it.char.toString(),
					it.rect.exactCenterX(),
					it.rect.exactCenterY() + 20f, // TODO: un-magic-ify this number
					paintArray[it.paintIndex]
			)
		}
	}

	lateinit var inputConnection: InputConnection
	private var downRunnable: Runnable? = null
	private var alreadyCommited = false
	// TODO: Make the click override or whatever
	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		if (event == null) return true
		val (char) = keyRegions.find { it.rect.contains(event.x.roundToInt(), event.y.roundToInt()) }
				?: return true
		when (event.action) {
			ACTION_DOWN -> {
				alreadyCommited = false
				downRunnable = Runnable {
					inputConnection.commitText(char.toUpperCase().toString(), 1)
					alreadyCommited = true
				}
				postDelayed(downRunnable, 1000) // TODO: un-magic-ify this and maybe move to own handler for own thread?
			}
			ACTION_UP -> {
				if (downRunnable != null) {
					removeCallbacks(downRunnable)
					downRunnable = null
				}
				if (alreadyCommited) return true
				if (char == '<') inputConnection.deleteSurroundingText(1, 0)
				else inputConnection.commitText(char.toString(), 1)
			}
			ACTION_MOVE -> Log.i(TAG, "Finger move")
			else -> Log.i(TAG, "What did I get? $event")
		}
		return true
	}


}
