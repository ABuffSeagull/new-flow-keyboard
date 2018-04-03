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
import android.support.graphics.drawable.VectorDrawableCompat
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import kotlin.math.roundToInt

/**
 * This is the View class for the keyboard, which shows everything on screen
 * Created by abuffseagull on 3/28/18.
 */

data class KeyRegion(val char: Char, val rect: Rect = Rect(), var paintIndex: Int = 0, var function: (() -> Unit)? = null)

const val KEY_LIST = "?xwvyb ,therm .caiolp#ksnudj z'gfq "

// row * 7 + column
const val BACKSPACE_INDEX = 0 * 7 + 6
const val SHIFT_INDEX = 4 * 7 + 0
const val ENTER_INDEX = 4 * 7 + 6
const val SPACE_INDEX = 1 * 7 + 6

class NewFlowView(context: Context) : View(context) {
	private val background = ShapeDrawable(RectShape())
	private val circleList = Array(5) { ShapeDrawable(OvalShape()) }
	private val keyRegions = Array(5 * 7) { KeyRegion(KEY_LIST[it]) }
	private var keyboardHeight = 0
	private val paintArray = Array(2) { Paint() }
	private var uppercase: Boolean = true
		set(value) {
			invalidate() // redraw the keyboard when changing case
			field = value
		}
	private val backspaceIcon = VectorDrawableCompat.create(resources, R.drawable.backspace_icon, null) as VectorDrawableCompat
	private val shiftIcon = VectorDrawableCompat.create(resources, R.drawable.shift_icon, null) as VectorDrawableCompat
	private val enterIcon = VectorDrawableCompat.create(resources, R.drawable.enter_icon, null) as VectorDrawableCompat
	private val spaceIcon = VectorDrawableCompat.create(resources, R.drawable.space_icon, null) as VectorDrawableCompat

	private var circleSize: Int = 0
		set(value) {
			keyboardHeight = value * 5
			field = value
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
		keyRegions[BACKSPACE_INDEX].function = { inputConnection.deleteSurroundingText(1, 0) }
		keyRegions[SHIFT_INDEX].function = { uppercase = !uppercase }
		keyRegions[ENTER_INDEX].function = { inputConnection.performEditorAction(EditorInfo.IME_ACTION_DONE) }
	}

	private fun getBounds(pair: Pair<Int, Int>, padding: Int = 0) = Rect(
			pair.first * circleSize + padding,
			pair.second * circleSize + padding,
			(pair.first + 1) * circleSize - padding,
			(pair.second + 1) * circleSize - padding
	)

	private fun getBounds(index: Int, padding: Int = 0) = Rect(
			(index % 7) * circleSize + padding,
			index / 7 * circleSize + padding,
			(index % 7 + 1) * circleSize - padding,
			(index / 7 + 1) * circleSize - padding
	)

	/**
	 * This is called during layout when the size of this view has changed.
	 * width and height SHOULD be the size of the entire screen, but not completely sure
	 */
	override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
		val padding = 5
		circleList[0].bounds = getBounds(Pair(3, 1), padding)
		circleList[1].bounds = getBounds(Pair(2, 2), padding)
		circleList[2].bounds = getBounds(Pair(3, 2), padding)
		circleList[3].bounds = getBounds(Pair(4, 2), padding)
		circleList[4].bounds = getBounds(Pair(4, 3), padding)
		background.setBounds(0, 0, width, height)
		keyRegions.forEachIndexed { i, keyRegion -> keyRegion.rect.set(getBounds(i)) }
		backspaceIcon.bounds = getBounds(BACKSPACE_INDEX, circleSize / 4)
		shiftIcon.bounds = getBounds(SHIFT_INDEX, circleSize / 4)
		enterIcon.bounds = getBounds(ENTER_INDEX, circleSize / 4)
		spaceIcon.bounds = getBounds(SPACE_INDEX, circleSize / 4)
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
					if (uppercase) it.char.toUpperCase().toString() else it.char.toString(),
					it.rect.exactCenterX(),
					it.rect.exactCenterY() + 20f, // TODO: un-magic-ify this number
					paintArray[it.paintIndex]
			)
		}
		backspaceIcon.draw(canvas)
		shiftIcon.draw(canvas)
		enterIcon.draw(canvas)
		spaceIcon.draw(canvas)
	}

	lateinit var inputConnection: InputConnection
	private var downRunnable: Runnable? = null
	private var alreadyCommitted = false
	// TODO: Make the click override or whatever
	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		if (event == null) return true
		val keyRegion = keyRegions.find { it.rect.contains(event.x.roundToInt(), event.y.roundToInt()) }
				?: return true
		when (event.action) {
			ACTION_DOWN -> {
				alreadyCommitted = false
				if (keyRegion.function != null) return true
				downRunnable = Runnable {
					inputConnection.commitText(keyRegion.char.toUpperCase().toString(), 1)
					alreadyCommitted = true
					uppercase = false
				}
				postDelayed(downRunnable, 1000) // TODO: un-magic-ify this and maybe move to own handler for own thread?
			}
			ACTION_UP -> {
				if (downRunnable != null) {
					removeCallbacks(downRunnable)
					downRunnable = null
				}
				if (alreadyCommitted) return true
				keyRegion.function?.let { it(); return true }
				inputConnection.commitText(if (uppercase) keyRegion.char.toUpperCase().toString() else keyRegion.char.toString(), 1)
				uppercase = false
			}
			ACTION_MOVE -> Log.i(TAG, "Finger move")
			else -> Log.i(TAG, "What did I get? $event")
		}
		return true
	}


}
