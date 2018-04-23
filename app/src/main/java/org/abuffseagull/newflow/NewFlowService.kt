package org.abuffseagull.newflow

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo

/**
 * Created by abuffseagull on 2/26/18.
 */
const val TAG = "FlowTag"

class NewFlowService : InputMethodService() {
	private lateinit var newFlowView: NewFlowView
	private lateinit var newFlowTouchListener: NewFlowTouchListener
	/**
	 * Create and return the view hierarchy used for the input area (such as
	 * a soft keyboard).  This will be called once, when the input area is
	 * first displayed.  You can return null to have no input area; the default
	 * implementation returns null.
	 *
	 *
	 * To control when the input view is displayed, implement
	 * [.onEvaluateInputViewShown].
	 * To change the input view after the first one is created by this
	 * function, use [.setInputView].
	 */
	@SuppressLint("ClickableViewAccessibility")
	override fun onCreateInputView(): View {
		newFlowView = NewFlowView(this)
		newFlowTouchListener = NewFlowTouchListener().apply { inputConnection = currentInputConnection }
		newFlowView.setOnTouchListener(newFlowTouchListener)
		return newFlowView
	} // = NewFlowView(this).apply { inputConnection = currentInputConnection }.also { newFlowView = it }

	/**
	 * Called when the input view is being shown and input has started on
	 * a new editor.  This will always be called after [.onStartInput],
	 * allowing you to do your general setup there and just view-specific
	 * setup here.  You are guaranteed that [.onCreateInputView] will
	 * have been called some time before this function is called.
	 *
	 * @param info Description of the type of text being edited.
	 * @param restarting Set to true if we are restarting input on the
	 * same text field as before.
	 */
	override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
		Log.i(TAG, "onStartInputView called, restarting: $restarting")
//		newFlowView.inputConnection = currentInputConnection
		newFlowTouchListener.inputConnection = currentInputConnection
		newFlowView.setToStartingState()
	}
}
