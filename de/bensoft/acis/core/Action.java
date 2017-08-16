/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

import java.util.ArrayList;

import de.bensoft.acis.core.environment.Environment;
import de.bensoft.acis.core.language.Sentence;

/**
 * Represents a Action for the {@link ACIS} system.
 * 
 * Actions are the key part of the system as they provide functionality.
 *
 */
public class Action {

	private String mName = "";
	private ActionPackage mPackage;
	private ContextVisibility mContextVisibility;
	private ActionMethod mActionMethod;
	private ActionParameter[] mActionParams = new ActionParameter[0];
	private String mTrigger;

	/**
	 * Creates a new Action Object.
	 * 
	 * In the {@code trigger} parameters are allowed to directly gather certain
	 * parts of the user input. Parameters are declared like in the following
	 * example: "say hello to &lt;&lt;p$0&gt;&gt; and &lt;&lt;p$1&gt;&gt;".
	 * Syntax: &lt;&lt;PARAMETERTYPE$INDEX&gt;&gt; Note: The parameter type
	 * currently has no effect, so any expression is allowed here (see
	 * {@link ParameterType}). The index specifies the order the parameters are
	 * given back in the {@link Action.ActionMethod} (should start at 0 counting
	 * up). When the same index occurs more than once or the specified parameter
	 * type does not exist the Action is considered as malformed and an
	 * {@link ActionMalformedException} is thrown.
	 * 
	 * @param actionName
	 *            The name of the Action.<br>
	 *            Format: &lt;NAME&gt;$&lt;ITERATION&gt;, while iteration is
	 *            used for e.g. multiple triggers.
	 * @param pack
	 *            The {@link ActionPackage} the Action belongs to.
	 * @param contextVisibility
	 *            The {@link ContextVisibility}.
	 * @param trigger
	 *            The trigger keyphrase.
	 * @param method
	 *            The {@link ActionMethod} containing the core functionality of
	 *            the Action.
	 * @throws ActionMalformedException
	 *             When the {@code trigger} is malformed.
	 */
	public Action(String actionName, ActionPackage pack, ContextVisibility contextVisibility, String trigger,
			ActionMethod method) throws ActionMalformedException {
		mName = pack.getName() + "/" + actionName;
		mPackage = pack;
		mContextVisibility = contextVisibility;
		mActionMethod = method;
		mTrigger = trigger;

		String[] parts = Sentence.splitUpAction(trigger);
		ArrayList<Integer> indexlist = new ArrayList<>();
		ArrayList<ActionParameter> parameters = new ArrayList<>();
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			if (part.startsWith("<<")) {
				try {
					String cont = part.substring(2, part.length() - 2);
					String[] cont2 = cont.split("\\$");

					ParameterType t = null;
					switch (cont2[0].trim()) {
					// case "p":
					default:
						t = ParameterType.EXPRESSION;
						break;
					/*
					 * default: throw new ActionMalformedException(actionName,
					 * "The Action could not be parsed: No valid ParameterType identifier in Action '"
					 * + actionName + "'.");
					 */
					}

					try {
						String trimmed = cont2[1].trim();
						if (trimmed.length() > 0) {
							int index = Integer.valueOf(trimmed);
							if (index >= 0 && !indexlist.contains(index)) {
								String predel = null;
								String postdel = null;
								if (i > 0) {
									predel = parts[i - 1];
								}
								if (i < parts.length - 1) {
									postdel = parts[i + 1];
								}
								indexlist.add(index);
								ActionParameter p = new ActionParameter(t, predel, postdel, index);
								parameters.add(p);
							} else {
								throw new Exception("Index is under 0 or already used in Action '" + actionName + "'.");
							}
						} else {
							throw new Exception("Index '" + trimmed + "' not valid in Action '" + actionName + "'.");
						}

					} catch (Exception ex) {
						throw new ActionMalformedException(
								"The Action '" + actionName + "' could not be parsed: " + ex.toString());
					}
				} catch (Exception ex) {
					throw new ActionMalformedException(actionName,
							"The Action '" + actionName + "' could not be parsed.", ex);
				}
			}
		}
		mActionParams = parameters.toArray(new ActionParameter[parameters.size()]);
	}

	/**
	 * Creates multiple Actions with same properties but different triggers.<br>
	 * This calls {@link Action#Action(String, ActionPackage, ContextVisibility, String, ActionMethod)} for each trigger.
	 * 
	 * @see Action#Action(String, ActionPackage, ContextVisibility, String,
	 *      ActionMethod)
	 * 
	 * @param actionName
	 *            The name of the Actions.
	 * @param pack
	 *            The {@link ActionPackage} the Actions belong to.
	 * @param contextVisibility
	 *            The {@link ContextVisibility}.
	 * @param triggers
	 *            Multiple trigger keyphrases.
	 * @param method
	 *            The {@link ActionMethod} containing the core functionality of
	 *            the Action.
	 * @return An array including the created Actions.
	 * @throws ActionMalformedException
	 *             When one or more {@code triggers} are malformed.
	 */
	public static Action[] getActions(String actionName, ActionPackage pack, ContextVisibility contextVisibility,
			String[] triggers, ActionMethod method) throws ActionMalformedException {
		ArrayList<Action> actions = new ArrayList<>(0);
		for (int i = 0; i < triggers.length; i++) {
			actions.add(new Action(actionName + "$" + i, pack, contextVisibility, triggers[i], method));
		}
		return actions.toArray(new Action[0]);
	}

	/**
	 * A function which returns the name of the Action.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return mName;
	}

	/**
	 * A function which returns the {@link ActionPackage} the Action belongs to.
	 * 
	 * @return The ActionPackage.
	 */
	public ActionPackage getPackage() {
		return mPackage;
	}

	/**
	 * A function which returns the {@link ContextVisibility} of the Action.
	 * 
	 * @return The context visibility.
	 */
	public ContextVisibility getContextVisibility() {
		return mContextVisibility;
	}

	/**
	 * A function which returns the {@link ActionMethod}.
	 * 
	 * @return The ActionMethod.
	 */
	public ActionMethod getActionMethod() {
		return mActionMethod;
	}

	/**
	 * A function which returns the parameters declared in the Action's trigger.
	 * 
	 * @return The {@link ActionParameter} array.
	 */
	public ActionParameter[] getActionParams() {
		return mActionParams;
	}

	/**
	 * A function which returns the trigger of the Action.
	 * 
	 * @return The trigger String including the parameters declaration.
	 */
	public String getTrigger() {
		return mTrigger;
	}

	/**
	 * This interface includes the run() function which contains the core code
	 * of the {@link Action}.
	 *
	 */
	public interface ActionMethod {

		/**
		 * This function is called when the Action should be executed.
		 * 
		 * @param environment
		 *            The {@link Environment} specifically for the
		 *            {@link ActionPackage} the {@link Action} belongs to.
		 * @param sentence
		 *            The user input {@link Sentence}.
		 * @param parameter
		 *            The parsed parameters array. See {@link Parameter} for
		 *            more information.
		 * @return An {@link ActionResult}.
		 */
		ActionResult run(final Environment environment, final Sentence sentence, final Parameter[] parameter);
	}
}