/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

/**
 * Represents a result of a comparison of an {@link Action} and the user input.
 *
 */
public class MatchResult {

	private Action mAction;
	private float mScore;
	private Parameter[] mParameters;

	/**
	 * The constructor.
	 * 
	 * @param action
	 *            The {@link Action} which was compared.
	 * @param score
	 *            The achieved score (between 0 and 1).
	 * @param parameter
	 *            The parsed {@link Parameter}s.
	 */
	public MatchResult(Action action, float score, Parameter[] parameter) {
		mAction = action;
		mScore = score;
		mParameters = parameter;
	}

	/**
	 * Returns the {@link Action} which was compared.
	 * 
	 * @return The {@link Action}.
	 */
	public Action getAction() {
		return mAction;
	}

	/**
	 * Returns the achieved score.
	 * 
	 * @return The score.
	 */
	public float getScore() {
		return mScore;
	}

	/**
	 * Returns the parsed {@link Parameter}s including their values.
	 * 
	 * @return The {@link Parameter} array.
	 */
	public Parameter[] getParameter() {
		return mParameters;
	}
}
