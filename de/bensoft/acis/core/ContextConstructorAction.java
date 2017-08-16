/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

/**
 * Represents an {@link Action} which constructs a context.
 *
 */
public class ContextConstructorAction extends Action {

	private String mContextId;
	private long mContextExpirationTime;

	/**
	 * Creates an {@link Action} which defines the constructor of a context.
	 * {@link ContextDependentAction}s with the same context id can only be
	 * found by the {@link Matcher} if a context created through such a context
	 * constructor exists.
	 * 
	 * @param contextId
	 *            The id of the context to be constructed. Must be unique in {@link ActionPackage}.
	 * @param contextExpirationTime
	 *            The time in milliseconds after which the context expires. -1
	 *            for infinity. Must not be {@code null}. {@inheritDoc}
	 * @see Action
	 */
	public ContextConstructorAction(String actionName, ActionPackage pack, String contextId, long contextExpirationTime,
			ContextVisibility contextVisibility, String trigger, ActionMethod method) throws ActionMalformedException {
		super(actionName, pack, contextVisibility, trigger, method);
		mContextId = pack.getName() + "#" + contextId;
		mContextExpirationTime = contextExpirationTime;
	}

	public String getContextId() {
		return mContextId;
	}

	public long getContextExpirationTime() {
		return mContextExpirationTime;
	}

}