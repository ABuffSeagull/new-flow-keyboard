package org.abuffseagull.newflow

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.inputmethod.EditorInfo

/**
 * Created by abuffseagull on 2/26/18.
 */
const val TAG = "FlowTag"

class NewFlowService : InputMethodService() {
	private lateinit var newFlowView: NewFlowView
	/**
	 * Create and return the view hierarchy used for the input area (such as a soft keyboard). This
	 * will be called once, when the input area is first displayed. You can return null to have
	 * no input area; the default implementation returns null.
	 *
	 * To control when the input view is displayed, implement onEvaluateInputViewShown(). To change
	 * the input view after the first one is created by this function, use setInputView(View).
	 */
	override fun onCreateInputView() = NewFlowView(this).apply { inputConnection = currentInputConnection }.also { newFlowView = it }

	/**
	 * Called when the input view is being shown and input has started on a new editor. This will
	 * always be called after onStartInput(EditorInfo, boolean), allowing you to do your general
	 * setup there and just view-specific setup here. You are guaranteed that onCreateInputView()
	 * will have been called some time before this function is called.
	 */
	override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
		Log.i(TAG, "onStartInputView called, restarting: $restarting")
		newFlowView.inputConnection = currentInputConnection
		newFlowView.setToStartingState()
	}
}
