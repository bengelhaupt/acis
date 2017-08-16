/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

/**
 * Represents an {@link Action} which depends on a specific context. It can only
 * be found by the {@link Matcher} if the specific context exists.
 *
 */
public class ContextDependentAction extends Action {

	private String mDependingContextId;

	/**
	 * Creates an {@link Action} which depends on a context.
	 * 
	 * @param dependingContextId
	 *            The id of the context to be constructed. {@inheritDoc}
	 * @see Action
	 */
	public ContextDependentAction(String actionName, ActionPackage pack, String dependingContextId,
			ContextVisibility contextVisibility, String trigger, ActionMethod method) throws ActionMalformedException {
		super(actionName, pack, contextVisibility, trigger, method);
		mDependingContextId = pack.getName() + "#" + dependingContextId;
	}

	public String getDependingContextId() {
		return mDependingContextId;
	}
}