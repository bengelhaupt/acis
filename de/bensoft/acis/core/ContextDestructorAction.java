/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

/**
 * Represents an {@link Action} which destroys a context.
 *
 */
public class ContextDestructorAction extends ContextDependentAction {

	/**
	 * Creates an {@link Action} which destroys a context.<br>
	 * Opposite of {@link ContextConstructorAction}.
	 * 
	 * @param contextId
	 *            The id of the context to be destroyed. {@inheritDoc}
	 * @see Action
	 */
	public ContextDestructorAction(String actionName, ActionPackage pack, String contextId,
			ContextVisibility contextVisibility, String trigger, ActionMethod method) throws ActionMalformedException {
		super(actionName, pack, contextId, contextVisibility, trigger, method);
	}
}