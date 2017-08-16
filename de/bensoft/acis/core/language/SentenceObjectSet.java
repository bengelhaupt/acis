/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core.language;

/**
 * Represents a set of {@link SentenceObject}s containing the categories ACTION,
 * WHAT, WHO, HOW and ELSE.<br>
 * See {@link Language#getSentenceObjects(Sentence)} for further information
 * regarding these categories.
 * 
 * @see Language#getSentenceObjects(Sentence)
 *
 */
public class SentenceObjectSet {

	private SentenceObject[] mActions = new SentenceObject[0];
	private SentenceObject[] mWhats = new SentenceObject[0];
	private SentenceObject[] mWhos = new SentenceObject[0];
	private SentenceObject[] mHows = new SentenceObject[0];
	private SentenceObject[] mElses = new SentenceObject[0];

	/**
	 * The constructor.
	 * 
	 * @param actions
	 *            The 'Action' {@link SentenceObject} array. Can be
	 *            {@code null}.
	 * @param whats
	 *            The 'What' {@link SentenceObject} array. Can be {@code null}.
	 * @param whos
	 *            The 'Who' {@link SentenceObject} array. Can be {@code null}.
	 * @param hows
	 *            The 'How' {@link SentenceObject} array. Can be {@code null}.
	 * @param elses
	 *            The 'Else' {@link SentenceObject} array. Can be {@code null}.
	 */
	public SentenceObjectSet(SentenceObject[] actions, SentenceObject[] whats, SentenceObject[] whos,
			SentenceObject[] hows, SentenceObject[] elses) {
		if (actions != null) {
			this.mActions = actions;
		}

		if (whats != null) {
			this.mWhats = whats;
		}

		if (whos != null) {
			this.mWhos = whos;
		}

		if (hows != null) {
			this.mHows = hows;
		}

		if (elses != null) {
			this.mElses = elses;
		}
	}

	/**
	 * Returns the 'Action' {@link SentenceObject}s.
	 * 
	 * @return An array containing all 'Action' {@link SentenceObject}s. When
	 *         there are no such {@link SentenceObject}s, the array is empty but
	 *         not {@code null}.
	 */
	public SentenceObject[] getActions() {
		return mActions;
	}

	/**
	 * Returns the 'What' {@link SentenceObject}s.
	 * 
	 * @return An array containing all 'Action' {@link SentenceObject}s. When
	 *         there are no such {@link SentenceObject}s, the array is empty but
	 *         not {@code null}.
	 */
	public SentenceObject[] getWhats() {
		return mWhats;
	}

	/**
	 * Returns the 'Who' {@link SentenceObject}s.
	 * 
	 * @return An array containing all 'Action' {@link SentenceObject}s. When
	 *         there are no such {@link SentenceObject}s, the array is empty but
	 *         not {@code null}.
	 */
	public SentenceObject[] getWhos() {
		return mWhos;
	}

	/**
	 * Returns the 'How' {@link SentenceObject}s.
	 * 
	 * @return An array containing all 'Action' {@link SentenceObject}s. When
	 *         there are no such {@link SentenceObject}s, the array is empty but
	 *         not {@code null}.
	 */
	public SentenceObject[] getHows() {
		return mHows;
	}

	/**
	 * Returns the 'Else' {@link SentenceObject}s.
	 * 
	 * @return An array containing all 'Action' {@link SentenceObject}s. When
	 *         there are no such {@link SentenceObject}s, the array is empty but
	 *         not {@code null}.
	 */
	public SentenceObject[] getElses() {
		return mElses;
	}
}