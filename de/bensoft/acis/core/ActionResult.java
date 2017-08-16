/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

/**
 * Represents a result of the execution of an {@link Action}. It contains an
 * {@link ActionResult.ActionResultCode} and may include a message.
 *
 */
public class ActionResult {

	private ActionResultCode mResultCode;
	private String mMessage;

	/**
	 * The constructor.
	 * 
	 * @param resultCode
	 *            The ActionResut representing the result of the {@link Action}.
	 */
	public ActionResult(ActionResultCode resultCode) {
		mResultCode = resultCode;
	}

	/**
	 * The constructor.
	 * 
	 * @param resultCode
	 *            The ActionResut representing the result of the {@link Action}.
	 * @param message
	 *            A message specifying the result more clearly.
	 */
	public ActionResult(ActionResultCode resultCode, String message) {
		mResultCode = resultCode;
		mMessage = message;
	}

	/**
	 * A function returning the ActionResultCode.
	 * 
	 * @return The result code.
	 */
	public ActionResultCode getResultCode() {
		return mResultCode;
	}

	/**
	 * A function returning the message if available.
	 * 
	 * @return The message. If no message is given, an empty String ("") will be
	 *         returned.
	 */
	public String getMessage() {
		if (mMessage == null)
			mMessage = "";
		return mMessage;
	}

	/**
	 * An enumeration containing the different result codes.<br>
	 * SUCCESS: The execution finished successful and delivered results.<br>
	 * RESULT_PENDING: The execution is not finished yet and results may be
	 * available in the future. (e.g. starting another Thread)<br>
	 * FAILURE: The execution ended with one or multiple errors and there are no
	 * results.<br>
	 * MISSING_INPUT: Some data is missing to complete the execution.<br>
	 * MISSING_FUNCTIONALITY: The Environment does not provide the functionality
	 * needed.<br>
	 * INTERNAL_ERROR: The execution ended unexpected and it is not the user's
	 * fault.<br>
	 * CREATE_CONTEXT: The context constructor returns this to indicate that a
	 * new context shall be created.<br>
	 * DESTROY_CONTEXT: The context constructor returns this to indicate that
	 * the context shall be destroyed.<br>
	 * OTHER: Any other problem that cannot be categorized into the above.
	 * Usually combined with a message.
	 *
	 */
	public static enum ActionResultCode {
		SUCCESS, RESULT_PENDING, FAILURE, MISSING_INPUT, MISSING_FUNCTIONALITY, INTERNAL_ERROR, CREATE_CONTEXT, DESTROY_CONTEXT, OTHER
	}
}