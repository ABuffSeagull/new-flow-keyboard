package org.abuffseagull.newflow

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.support.graphics.drawable.VectorDrawableCompat
import android.view.View

/**
 * This is the square region that encompasses a keyboard key
 * @param char The character this key represents
 * @param rect The bounding box for the key
 * @param paintIndex The index for the paint array, to get it's color
 */
data class KeyRegion(val char: Char, val rect: Rect = Rect(), var paintIndex: Int = 0)

/**
 * The list of primary keyboard keys in the order of the keyboard
 */
const val KEY_LIST_PRIMARY: String = "?xwvyb ,therm .caiolp#ksnudj z'gfq "
/**
 * The list of secondary keyboard keys in the order of the keyboard
 */
const val KEY_LIST_SECONDARY: String = "!@()%   :123/  ;456+& $789-  =\"0#* "
/**
 * How many columns the keyboard has
 */
const val KEYBOARD_ROW_LENGTH: Int = 7
/**
 * How many rows the keyboard has
 */
const val KEYBOARD_HEIGHT_SIZE: Int = 5 // TODO: come up with a better name

/**
 *
 */// row * KEYBOARD_ROW_LENGTH + column
const val BACKSPACE_INDEX: Int = 0 * KEYBOARD_ROW_LENGTH + 6
/**
 *
 */
const val SHIFT_INDEX: Int = 4 * KEYBOARD_ROW_LENGTH + 0
/**
 *
 */
const val ENTER_INDEX: Int = 4 * KEYBOARD_ROW_LENGTH + 6
/**
 *
 */
const val SPACE_INDEX: Int = 1 * KEYBOARD_ROW_LENGTH + 6
/**
 *
 */
const val SECONDARY_INDEX: Int = 3 * KEYBOARD_ROW_LENGTH + 0

/**
 * This is the main view for the entire keyboard
 * @param context The context for view
 */
class NewFlowView(context: Context) : View(context) {
	private val background = ShapeDrawable(RectShape())
	private val circleList = Array(5) { ShapeDrawable(OvalShape()) } // 5 cause vowels
	private val keyRegionsPrimary =
		Array(KEYBOARD_HEIGHT_SIZE * KEYBOARD_ROW_LENGTH) { KeyRegion(KEY_LIST_PRIMARY[it]) }
	private val keyRegionsSecondary =
		Array(KEYBOARD_HEIGHT_SIZE * KEYBOARD_ROW_LENGTH) { KeyRegion(KEY_LIST_SECONDARY[it]) }
	private var keyboardHeight = 0
	private val paintArray = Array(4) { Paint() }
	/**
	 *
	 */
	var uppercaseToggle: Boolean = true
		set(value) {
			invalidate() // redraw the keyboard when changing case
			field = value
		}
	/**
	 *
	 */
	var secondaryToggle: Boolean = false
		set(value) {
			invalidate()
			field = value
		}
	private val iconBackspace = VectorDrawableCompat.create(
		resources,
		R.drawable.backspace_icon,
		null
	) as VectorDrawableCompat
	private val iconShift =
		VectorDrawableCompat.create(resources, R.drawable.shift_icon, null) as VectorDrawableCompat
	private val iconEnter =
		VectorDrawableCompat.create(resources, R.drawable.enter_icon, null) as VectorDrawableCompat
	private val iconSpace =
		VectorDrawableCompat.create(resources, R.drawable.space_icon, null) as VectorDrawableCompat

	private var circleSize: Int = 0
		set(value) {
			keyboardHeight = value * 5
			field = value
		}

	init {
		val colorArray = context.theme.obtainStyledAttributes(
			intArrayOf(
				android.R.attr.colorBackground,
				android.R.attr.textColorPrimary,
				android.R.attr.textColorPrimaryInverse,
				android.R.attr.textColorSecondary,
				android.R.attr.textColorSecondaryInverse
			)
		)
		background.paint.color = colorArray.getColor(0, 0xFF00FF)
		paintArray.forEachIndexed { index, paint ->
			paint.color = colorArray.getColor(index + 1, 0xFF00FF)
		}
		colorArray.recycle()

		circleList.forEach {
			@Suppress("DEPRECATION")
			it.paint.color = resources.getColor(R.color.primary)
		}
		for (paint in paintArray) {
			paint.textAlign = Paint.Align.CENTER
			paint.flags = Paint.ANTI_ALIAS_FLAG
		}
		// NOTE: should these be sized according to the screen? or maybe circle size?
		paintArray[0].textSize = 70f
		paintArray[1].textSize = 70f
		paintArray[2].textSize = 50f
		paintArray[3].textSize = 50f
		keyRegionsPrimary.filter { "aeiou".contains(it.char) }.forEach { it.paintIndex = 1 }
		keyRegionsSecondary.filter { "24569".contains(it.char) }.forEach { it.paintIndex = 1 }
	}

	/**
	 * Sets it back to the primary keys, and in uppercase
	 */
	fun setToStartingState() {
		uppercaseToggle = true
		secondaryToggle = false
	}

	/**
	 *
	 */
	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		getVowelBounds()
		background.setBounds(0, 0, width, height)
		keyRegionsPrimary.forEachIndexed { i, keyRegion -> keyRegion.rect.set(getBoundsFromIndex(i)) }
		keyRegionsSecondary.forEachIndexed { index, keyRegion ->
			keyRegion.rect.set(
				getBoundsFromIndex(index)
			)
		}
		getIconBounds()
	}

	private fun getIconBounds() {
		iconBackspace.bounds = getBoundsFromIndex(BACKSPACE_INDEX, circleSize / 4)
		iconShift.bounds = getBoundsFromIndex(SHIFT_INDEX, circleSize / 4)
		iconEnter.bounds = getBoundsFromIndex(ENTER_INDEX, circleSize / 4)
		iconSpace.bounds = getBoundsFromIndex(SPACE_INDEX, circleSize / 4)
	}

	private fun getBoundsFromIndex(index: Int, padding: Int = 0) = Rect(
		(index % KEYBOARD_ROW_LENGTH) * circleSize + padding,
		(index / KEYBOARD_ROW_LENGTH) * circleSize + padding,
		(index % KEYBOARD_ROW_LENGTH + 1) * circleSize - padding,
		(index / KEYBOARD_ROW_LENGTH + 1) * circleSize - padding
	)

	private fun getVowelBounds() {
		val padding = 5
		circleList[0].bounds = getBoundsFromPair(Pair(3, 1), padding)
		circleList[1].bounds = getBoundsFromPair(Pair(2, 2), padding)
		circleList[2].bounds = getBoundsFromPair(Pair(3, 2), padding)
		circleList[3].bounds = getBoundsFromPair(Pair(4, 2), padding)
		circleList[4].bounds = getBoundsFromPair(Pair(4, 3), padding)
	}

	private fun getBoundsFromPair(pair: Pair<Int, Int>, padding: Int = 0) = Rect(
		pair.first * circleSize + padding,
		pair.second * circleSize + padding,
		(pair.first + 1) * circleSize - padding,
		(pair.second + 1) * circleSize - padding
	)


	/**
	 *
	 */
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val screenWidth = Resources.getSystem().displayMetrics.widthPixels
		circleSize = screenWidth / KEYBOARD_ROW_LENGTH
		setMeasuredDimension(Resources.getSystem().displayMetrics.widthPixels, circleSize * 5)
	}

	/**
	 *
	 */
	override fun onDraw(canvas: Canvas?) {
		if (canvas == null) return
		background.draw(canvas)
		circleList.forEach { it.draw(canvas) }
		drawText(canvas)
		drawIcons(canvas)
	}

	private fun drawIcons(canvas: Canvas) {
		iconBackspace.draw(canvas)
		iconShift.draw(canvas)
		iconEnter.draw(canvas)
		iconSpace.draw(canvas)
	}

	private fun drawText(canvas: Canvas) {
		// TODO: these numbers suck, un-magic-ify them
		// NOTE: Look into static layouts, maybe better for performance?
		val primaryDisplayArray = if (!secondaryToggle) keyRegionsPrimary else keyRegionsSecondary
		val secondaryDisplayArray = if (!secondaryToggle) keyRegionsSecondary else keyRegionsPrimary
		for ((char, rect, paintIndex) in primaryDisplayArray) {
			canvas.drawText(
				(if (uppercaseToggle) char.toUpperCase() else char).toString(),
				rect.exactCenterX() - 10f,
				rect.exactCenterY() + 35f,
				paintArray[paintIndex]
			)
		}
		for ((char, rect, paintIndex) in secondaryDisplayArray) {
			canvas.drawText(
				(if (uppercaseToggle) char.toUpperCase() else char).toString(),
				rect.exactCenterX() + 35f,
				rect.exactCenterY() - 5f,
				paintArray[paintIndex + 2]
			)
		}
	}

	/**
	 * @param coordinates The x,y coordinates of the touch
	 * @return the index of the [KeyRegion]
	 */
	fun find(coordinates: Pair<Int, Int>): Int =
		keyRegionsPrimary.indexOfFirst { it.rect.contains(coordinates.first, coordinates.second) }

	/**
	 * @param indexFound The index of the [KeyRegion] that was touched
	 * @return [Pair] of chars, of the primary and secondary regions
	 */
	fun getPrimaryAndSecondaryChars(indexFound: Int): Pair<Char, Char> = Pair(
		(if (secondaryToggle) keyRegionsSecondary[indexFound] else keyRegionsPrimary[indexFound]).char,
		(if (secondaryToggle) keyRegionsPrimary[indexFound] else keyRegionsSecondary[indexFound]).char
	)
}
