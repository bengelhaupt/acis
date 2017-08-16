/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

/**
 * Represents an context item in a {@link ContextStack}.
 *
 */
public class ContextStackItem {

	private long mTime;
	private Action mAction;
	private String mInput;
	private ActionResult mResult;

	/**
	 * The constructor.
	 * 
	 * @param time
	 *            The time of Action execution.
	 * @param action
	 *            The {@link Action}
	 * @param input
	 *            The whole user input String.
	 * @param result
	 *            The {@link ActionResult} the {@link Action} returned after
	 *            execution.
	 */
	public ContextStackItem(long time, Action action, String input, ActionResult result) {
		mTime = time;
		mAction = action;
		mInput = input;
		mResult = result;
	}

	/**
	 * Returns the age of the item.<br>
	 * This is the difference between {@link System#currentTimeMillis()} and
	 * {@link #getTime()}.
	 * 
	 * @return The age in milliseconds.
	 */
	public long getAge() {
		return System.currentTimeMillis() - mTime;
	}

	/**
	 * Returns the time of execution.
	 * 
	 * @return The time in milliseconds.
	 */
	public long getTime() {
		return mTime;
	}

	/**
	 * Returns the {@link Action}.
	 * 
	 * @return The Action.
	 */
	public Action getAction() {
		return mAction;
	}

	/**
	 * Returns the user input String.
	 * 
	 * @return The whole user input including any parameters.
	 */
	public String getInputString() {
		return mInput;
	}

	/**
	 * Returns the {@link ActionResult} returned by the {@link Action}'s
	 * {@link Action.ActionMethod} after execution.
	 * 
	 * @return The ActionResult.
	 */
	public ActionResult getActionResult() {
		return mResult;
	}
}