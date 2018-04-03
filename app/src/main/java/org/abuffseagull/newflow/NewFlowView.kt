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
const val KEY_LIST_SECONDARY = "!@()%   :123/  ;456+& $789-  =\"0#* "

// row * 7 + column
const val BACKSPACE_INDEX = 0 * 7 + 6
const val SHIFT_INDEX = 4 * 7 + 0
const val ENTER_INDEX = 4 * 7 + 6
const val SPACE_INDEX = 1 * 7 + 6
const val SECONDARY_INDEX = 3 * 7 + 0

class NewFlowView(context: Context) : View(context) {
	private val background = ShapeDrawable(RectShape())
	private val circleList = Array(5) { ShapeDrawable(OvalShape()) }
	private val keyRegions = Array(5 * 7) { KeyRegion(KEY_LIST[it]) }
	private val keyRegionsSecondary = Array(5 * 7) { KeyRegion(KEY_LIST_SECONDARY[it]) }
	private var keyboardHeight = 0
	private val paintArray = Array(4) { Paint() }
	private var uppercaseToggle: Boolean = true
		set(value) {
			invalidate() // redraw the keyboard when changing case
			field = value
		}
	private var secondaryToggle: Boolean = false
		set(value) {
			invalidate()
			field = value
		}
	private val iconBackspace = VectorDrawableCompat.create(resources, R.drawable.backspace_icon, null) as VectorDrawableCompat
	private val iconShift = VectorDrawableCompat.create(resources, R.drawable.shift_icon, null) as VectorDrawableCompat
	private val iconEnter = VectorDrawableCompat.create(resources, R.drawable.enter_icon, null) as VectorDrawableCompat
	private val iconSpace = VectorDrawableCompat.create(resources, R.drawable.space_icon, null) as VectorDrawableCompat

	private var circleSize: Int = 0
		set(value) {
			keyboardHeight = value * 5
			field = value
		}

	init {
		val colorArray = context.theme.obtainStyledAttributes(intArrayOf(
				android.R.attr.colorBackground,
				android.R.attr.textColorPrimary,
				android.R.attr.textColorPrimaryInverse,
				android.R.attr.textColorSecondary,
				android.R.attr.textColorSecondaryInverse
		))
		background.paint.color = colorArray.getColor(0, 0xFF00FF)
		paintArray.forEachIndexed { index, paint -> paint.color = colorArray.getColor(index + 1, 0xFF00FF) }
		colorArray.recycle()

		circleList.forEach {
			@Suppress("DEPRECATION")
			it.paint.color = resources.getColor(R.color.primary)
		}
		for (paint in paintArray) {
			paint.textAlign = Paint.Align.CENTER
			paint.flags = Paint.ANTI_ALIAS_FLAG
		}
		// TODO: should these be sized according to the screen?
		paintArray[0].textSize = 70f
		paintArray[1].textSize = 70f
		paintArray[2].textSize = 50f
		paintArray[3].textSize = 50f
		keyRegions.filter { "aeiou".contains(it.char) }.forEach { it.paintIndex = 1 }
		keyRegionsSecondary.filter { "24569".contains(it.char) }.forEach { it.paintIndex = 1 }
		keyRegions[BACKSPACE_INDEX].function = { inputConnection.deleteSurroundingText(1, 0) }
		keyRegions[SHIFT_INDEX].function = { uppercaseToggle = !uppercaseToggle }
		keyRegions[ENTER_INDEX].function = { inputConnection.performEditorAction(EditorInfo.IME_ACTION_DONE) }
		keyRegions[SECONDARY_INDEX].function = { secondaryToggle = !secondaryToggle }
	}

	private fun getBounds(pair: Pair<Int, Int>, padding: Int = 0) = Rect(
			pair.first * circleSize + padding,
			pair.second * circleSize + padding,
			(pair.first + 1) * circleSize - padding,
			(pair.second + 1) * circleSize - padding
	)

	private fun getBounds(index: Int, padding: Int = 0) = Rect(
			(index % 7) * circleSize + padding,
			(index / 7) * circleSize + padding,
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
		keyRegionsSecondary.forEachIndexed { index, keyRegion -> keyRegion.rect.set(getBounds(index)) }
		iconBackspace.bounds = getBounds(BACKSPACE_INDEX, circleSize / 4)
		iconShift.bounds = getBounds(SHIFT_INDEX, circleSize / 4)
		iconEnter.bounds = getBounds(ENTER_INDEX, circleSize / 4)
		iconSpace.bounds = getBounds(SPACE_INDEX, circleSize / 4)
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
		// TODO: these numbers suck, un-magic-ify them
		// TODO: Look into static layouts, maybe better for performance?
		val primaryDisplayArray = if (secondaryToggle) keyRegionsSecondary else keyRegions
		val secondaryDisplayArray = if (secondaryToggle) keyRegions else keyRegionsSecondary
		primaryDisplayArray.forEach {
			canvas.drawText(
					(if (uppercaseToggle) it.char.toUpperCase() else it.char).toString(),
					it.rect.exactCenterX() - 10f,
					it.rect.exactCenterY() + 35f,
					paintArray[it.paintIndex]
			)
		}
		secondaryDisplayArray.forEach {
			canvas.drawText(
					(if (uppercaseToggle) it.char.toUpperCase() else it.char).toString(),
					it.rect.exactCenterX() + 35f,
					it.rect.exactCenterY() - 5f,
					paintArray[it.paintIndex + 2]
			)
		}
		iconBackspace.draw(canvas)
		iconShift.draw(canvas)
		iconEnter.draw(canvas)
		iconSpace.draw(canvas)
	}

	lateinit var inputConnection: InputConnection
	private var downRunnable: Runnable? = null
	private var alreadyCommitted = false
	// TODO: Make the click override or whatever
	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		if (event == null) return true
		val indexFound = keyRegions.indexOfFirst { it.rect.contains(event.x.roundToInt(), event.y.roundToInt()) }
		if (indexFound < 0) return true
		val keyCharPrimary = (if (secondaryToggle) keyRegionsSecondary[indexFound] else keyRegions[indexFound]).char
		val keyCharSecondary = (if (secondaryToggle) keyRegions[indexFound] else keyRegionsSecondary[indexFound]).char
		val keyFunction = keyRegions[indexFound].function
				?: keyRegionsSecondary[indexFound].function
		when (event.action) {
			ACTION_DOWN -> {
				alreadyCommitted = false
				if (keyFunction != null) return true
				downRunnable = Runnable {
					inputConnection.commitText(keyCharSecondary.toString(), 1)
					alreadyCommitted = true
					uppercaseToggle = false
				}
				postDelayed(downRunnable, 1000) // TODO: un-magic-ify this and maybe move to own handler for own thread?
			}
			ACTION_UP -> {
				if (downRunnable != null) {
					removeCallbacks(downRunnable)
					downRunnable = null
				}
				if (alreadyCommitted) return true
				keyFunction?.let { it(); return true }
				inputConnection.commitText(if (uppercaseToggle) keyCharPrimary.toUpperCase().toString() else keyCharPrimary.toString(), 1)
				uppercaseToggle = false
			}
			ACTION_MOVE -> Log.i(TAG, "Finger move")
			else -> Log.i(TAG, "What did I get? $event")
		}
		return true
	}


}
