/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

/**
 * Represents a Exception which is thrown when the Action is malformed. This is
 * the case when a index occurs more than once or the specified parameter type
 * identifier is invalid. See ParameterType.
 *
 */
public final class ActionMalformedException extends Exception {

	private static final long serialVersionUID = 3457114308952568498L;

	private String mActionName;

	/**
	 * Exception constructor.
	 * 
	 * @param actionName
	 *            The name of the {@link Action}.
	 */
	public ActionMalformedException(String actionName) {
		mActionName = actionName;
	}

	/**
	 * Exception constructor.
	 * 
	 * @param actionName
	 *            The name of the {@link Action}.
	 * @param message
	 *            The Exception message.
	 */
	public ActionMalformedException(String actionName, String message) {
		super(message);
		mActionName = actionName;
	}

	/**
	 * Exception constructor.
	 * 
	 * @param actionName
	 *            The name of the {@link Action}.
	 * @param message
	 *            The Exception message.
	 * @param inner
	 *            The inner Exception.
	 */
	public ActionMalformedException(String actionName, String message, Throwable inner) {
		super(message, inner);
		mActionName = actionName;
	}

	/**
	 * A function which returns the name of the {@link Action} with the
	 * malformed trigger.
	 * 
	 * @return The name of the Action.
	 */
	public String getActionName() {
		return mActionName;
	}
}