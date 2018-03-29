package org.abuffseagull.newflow

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

	override fun onCreate() {
		Log.i(TAG, "onCreate called\n")
		super.onCreate()
	}

	/**
	 * This is a hook that subclasses can use to perform initialization of their interface. It is
	 * called for you prior to any of your UI objects being created, both after the service is
	 * first created and after a configuration change happens.
	 */
	override fun onInitializeInterface() {
		Log.i(TAG, "onInitializeInterface called\n")
		super.onInitializeInterface()
	}

	/**
	 * Called when a new client has bound to the input method. This may be followed by a series of
	 * onStartInput(EditorInfo, boolean) and onFinishInput() calls as the user navigates through
	 * its UI. Upon this call you know that getCurrentInputBinding() and
	 * getCurrentInputConnection() return valid objects.
	 */
	override fun onBindInput() {
		Log.i(TAG, "onBindInput called\n")
		super.onBindInput()
	}

	/**
	 * Called to inform the input method that text input has started in an editor. You should use
	 * this callback to initialize the state of your input to match the state of the editor
	 * given to it.
	 */
	override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
		Log.i(TAG, "onStartInput called, restarting: $restarting")
		super.onStartInput(attribute, restarting)
	}

	/**
	 * Create and return the view hierarchy used for the input area (such as a soft keyboard). This
	 * will be called once, when the input area is first displayed. You can return null to have
	 * no input area; the default implementation returns null.
	 *
	 * To control when the input view is displayed, implement onEvaluateInputViewShown(). To change
	 * the input view after the first one is created by this function, use setInputView(View).
	 */
	override fun onCreateInputView(): View {
		Log.i(TAG, "onCreateInputView called\n")
//		return mView // This creates the view the first time, cause it is lazy
		newFlowView = NewFlowView(this)
		return newFlowView
	}

	/**
	 * Create and return the view hierarchy used to show candidates. This will be called once,
	 * when the candidates are first displayed. You can return null to have no candidates view;
	 * the default implementation returns null.
	 * To control when the candidates view is displayed, use setCandidatesViewShown(boolean).
	 * To change the candidates view after the first one is created by this function, use
	 * setCandidatesView(View).
	 */
	override fun onCreateCandidatesView(): View? {
		Log.i(TAG, "onCreateCandidatesView called\n")
		return null
	}


	/**
	 * Called when the input view is being shown and input has started on a new editor. This will
	 * always be called after onStartInput(EditorInfo, boolean), allowing you to do your general
	 * setup there and just view-specific setup here. You are guaranteed that onCreateInputView()
	 * will have been called some time before this function is called.
	 */
	override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
		Log.i(TAG, "onStartInputView called, restarting: $restarting")
		super.onStartInputView(info, restarting)
		newFlowView = NewFlowView(this)
		setInputView(newFlowView) // TODO: This is for debug purposes, remove/replace
	}


	/**
	 * Called by the framework to create the layout for showing extracted text. Only called when
	 * in fullscreen mode. The returned view hierarchy must have an ExtractEditText whose ID is
	 * inputExtractEditText.
	 */
	override fun onCreateExtractTextView(): View {
		Log.i(TAG, "onCreateExtractTextView called\n")
		return super.onCreateExtractTextView()
	}

	/**
	 * Called to inform the input method that text input has finished in the last editor. At this
	 * point there may be a call to onStartInput(EditorInfo, boolean) to perform input in a new
	 * editor, or the input method may be left idle. This method is not called when input restarts
	 * in the same editor.
	 *
	 * The default implementation uses the InputConnection to clear any active composing text; you
	 * can override this (not calling the base class implementation) to perform whatever behavior
	 * you would like.
	 */
	override fun onFinishInput() {
		Log.i(TAG, "onFinishInput called\n")
		super.onFinishInput()
	}

	/**
	 * Called by the system to notify a Service that it is no longer used and is being removed.
	 * The service should clean up any resources it holds (threads, registered receivers, etc) at
	 * this point. Upon return, there will be no more calls in to this Service object and it is
	 * effectively dead. Do not call this method directly.
	 */
//	override fun onDestroy() {
//		Log.i(TAG, "onDestroy called\n")
//		super.onDestroy()
//	}
}
