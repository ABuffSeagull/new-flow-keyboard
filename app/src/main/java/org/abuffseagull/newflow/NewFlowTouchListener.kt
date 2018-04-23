package org.abuffseagull.newflow

import android.annotation.SuppressLint
import android.os.Handler
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import kotlin.math.roundToInt

class NewFlowTouchListener : View.OnTouchListener {
	lateinit var inputConnection: InputConnection
	private val handlerThread = Handler()
	private var keyHeldAction: Runnable? = null
	private var textAlreadyCommitted = false
	private var indexFound = 0
	private var keyFunction: (() -> Unit)? = null
	private lateinit var view: NewFlowView
	/**
	 * Called when a touch event is dispatched to a view. This allows listeners to
	 * get a chance to respond before the target view.
	 *
	 * @param v The view the touch event has been dispatched to.
	 * @param event The MotionEvent object containing full information about
	 * the event.
	 * @return True if the listener has consumed the event, false otherwise.
	 */
	@SuppressLint("ClickableViewAccessibility")
	override fun onTouch(v: View?, event: MotionEvent?): Boolean {
		if (v == null || event == null) return true
		view = v as NewFlowView
		indexFound = view.find(Pair(event.x.roundToInt(), event.y.roundToInt())) // TODO: rename
		if (indexFound < 0) return true
		val (keyCharPrimary, keyCharSecondary) = view.getPrimaryAndSecondaryChars(indexFound)
		keyFunction = when (indexFound) {
			BACKSPACE_INDEX -> {
				{ inputConnection.deleteSurroundingText(1, 0) }
			}
			SHIFT_INDEX -> {
				{ view.uppercaseToggle = !view.uppercaseToggle }
			}
			ENTER_INDEX -> {
				{ inputConnection.performEditorAction(EditorInfo.IME_ACTION_DONE) }
			}
			SECONDARY_INDEX -> {
				{ view.secondaryToggle = !view.secondaryToggle }
			}
			else -> null
		}
		return when (event.action) {
			ACTION_DOWN -> handleActionDown(keyCharSecondary)
			ACTION_UP -> handleActionUp(keyCharPrimary)
			else -> false
		}
	}

	private fun handleActionDown(keyCharSecondary: Char): Boolean {
		textAlreadyCommitted = false
		if (indexFound == BACKSPACE_INDEX) {
			keyHeldAction = Runnable {
				inputConnection.deleteSurroundingText(1, 0)
				handlerThread.postDelayed(keyHeldAction, 100) // TODO: un-magic-ify
			}
			handlerThread.postDelayed(keyHeldAction, 500) // TODO: un-magic-ify
		}
		if (keyFunction != null) return true
		keyHeldAction = Runnable {
			inputConnection.commitText(keyCharSecondary.toString(), 1)
			textAlreadyCommitted = true
			view.uppercaseToggle = false
		}
		handlerThread.postDelayed(keyHeldAction, 500) // TODO: un-magic-ify
		return true
	}

	private fun handleActionUp(keyCharPrimary: Char): Boolean {
		if (keyHeldAction != null) {
			handlerThread.removeCallbacks(keyHeldAction)
			keyHeldAction = null
		}
		if (textAlreadyCommitted) return true
		keyFunction?.let { it(); return true }
		inputConnection.commitText((if (view.uppercaseToggle) keyCharPrimary.toUpperCase() else keyCharPrimary).toString(), 1)
		view.uppercaseToggle = false
		return true
	}

}