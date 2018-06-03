package org.abuffseagull.newflow

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.inputmethod.EditorInfo


/**
 * TAG for debugging
 */
const val TAG = "FlowTag"

/**
 * The main service for the keyboard
 */
class NewFlowService : InputMethodService() {
	private lateinit var newFlowView: NewFlowView
	private lateinit var newFlowTouchListener: NewFlowTouchListener
	override fun onCreate() {
		super.onCreate()
		newFlowTouchListener =
				NewFlowTouchListener() // NOTE: Should this go here, or sometime later?
	}

	override fun onCreateInputView() = NewFlowView(this).also { newFlowView = it }

	override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
		if (BuildConfig.DEBUG) Log.i(TAG, "onStartInput called, restarting $restarting")
		newFlowTouchListener.inputConnection = currentInputConnection
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
		if (BuildConfig.DEBUG) Log.i(TAG, "onStartInputView called, restarting: $restarting")
		newFlowView.setOnTouchListener(newFlowTouchListener)
		newFlowView.setToStartingState()
	}
}
