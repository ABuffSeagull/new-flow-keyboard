package org.abuffseagull.newflow

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.inputmethod.EditorInfo

/**
 * Created by abuffseagull on 2/26/18.
 */
const val TAG = "FlowTag"

class NewFlowService : InputMethodService() {
	private lateinit var newFlowView: NewFlowView
	private lateinit var newFlowTouchListener: NewFlowTouchListener
	override fun onCreate() {
		super.onCreate()
		newFlowTouchListener =
				NewFlowTouchListener() // NOTE: Should this go here, or sometime later?
	}

	override fun onCreateInputView() = NewFlowView(this).also { newFlowView = it }
	/**
	 * Called to inform the input method that text input has started in an
	 * editor.  You should use this callback to initialize the state of your
	 * input to match the state of the editor given to it.
	 *
	 * @param attribute The attributes of the editor that input is starting
	 * in.
	 * @param restarting Set to true if input is restarting in the same
	 * editor such as because the application has changed the text in
	 * the editor.  Otherwise will be false, indicating this is a new
	 * session with the editor.
	 */
	override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
		Log.i(TAG, "onStartInput called, restarting $restarting")
		newFlowTouchListener.inputConnection = currentInputConnection
	}

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
	@SuppressLint("ClickableViewAccessibility")
	override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
		Log.i(TAG, "onStartInputView called, restarting: $restarting")
		newFlowView.setOnTouchListener(newFlowTouchListener)
		newFlowView.setToStartingState()
	}
}
