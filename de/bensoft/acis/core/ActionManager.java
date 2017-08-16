/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.bensoft.acis.utils.Logging.Logger;

/**
 * The ActionManager holds the {@link Action}s of the {@link ACIS} system it is
 * tied to.
 *
 */
public class ActionManager {

	private static final String LOG_TAG = "ACTIONS";

	private List<Action> mActions = new ArrayList<>();
	private Logger mLogger;

	/**
	 * The constructor.
	 * 
	 * @param logger
	 *            The {@link de.bensoft.acis.utils.Logging.Logger} to use.
	 */
	public ActionManager(Logger logger) {
		mLogger = logger;
	}

	/**
	 * Adds an {@link Action} to the system.<br>
	 * Note: {@link Action}s with a minimum required library version higher than
	 * the this' {@link ACIS#LIBRARY_VERSION} are not added.
	 * 
	 * @param action
	 *            The {@link Action} to add.
	 */
	public void add(Action action) {
		if (action.getPackage().getMinimumRequiredLibraryVersion() <= ACIS.LIBRARY_VERSION) {
			mActions.add(action);
			mLogger.i(LOG_TAG, "Action '" + action.getName() + "' successfully initialized and added to the system.");
		} else {
			mLogger.w(LOG_TAG,
					"Action '" + action.getName()
							+ "' was not added to the system because it requires the minimum library version "
							+ action.getPackage().getMinimumRequiredLibraryVersion() + ".");
		}
	}

	/**
	 * Adds an array of {@link Action}s to the system. Note: {@link Action}s
	 * with a minimum required library version higher than the this'
	 * {@link ACIS#LIBRARY_VERSION} are not added.
	 * 
	 * @param actions
	 *            The {@link Action} array.
	 */
	public void add(Action[] actions) {
		for (Action action : actions)
			add(action);
	}

	/**
	 * Returns all {@link Action}s registered in the system.
	 * 
	 * @return The {@link Action} array.
	 */
	public Action[] getActions() {
		return mActions.toArray(new Action[0]);
	}

	/**
	 * Returns all {@link Action}s except {@link ContextDependentAction}s and
	 * {@link ContextDestructorAction}s for which no context exists or where the
	 * context is expired.
	 * 
	 * @param contexts
	 *            An array of context ids.
	 * 
	 * @return The {@link Action} array including
	 *         {@link ContextDependentAction}s and
	 *         {@link ContextDestructorAction}s for the context ids given in
	 *         {@code contexts}.
	 */
	Action[] getActions(Map<String, Context> contexts) {
		List<Action> actions = new ArrayList<>(0);
		for (int i = 0; i < mActions.size(); i++) {
			Action action = mActions.get(i);
			if (action instanceof ContextDependentAction) {
				ContextDependentAction cdAction = (ContextDependentAction) action;
				if (contexts.containsKey(cdAction.getDependingContextId())) {
					Context context = contexts.get(cdAction.getDependingContextId());
					if (context.getExpirationTime() == -1
							|| context.getCreationTime() + context.getExpirationTime() >= System.currentTimeMillis())
						actions.add(action);
				}
			}
			if (!(action instanceof ContextDependentAction)) {
				actions.add(action);
			}
		}
		return actions.toArray(new Action[0]);
	}

	/**
	 * Returns the {@link ActionPackage}s registered in the system.
	 * 
	 * @return The {@link ActionPackage} array.
	 */
	public ActionPackage[] getActionPackages() {
		List<ActionPackage> packs = new ArrayList<>(0);
		for (Action a : getActions())
			if (!packs.contains(a.getPackage()))
				packs.add(a.getPackage());
		return packs.toArray(new ActionPackage[0]);
	}

	/**
	 * Removes all {@link Action}s from the system.
	 */
	public void removeAll() {
		for (int i = 0; i < mActions.size(); i++)
			mActions.remove(i);
	}
}