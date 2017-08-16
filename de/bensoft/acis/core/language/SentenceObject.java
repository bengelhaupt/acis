/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core.language;

/**
 * Represents an object in a sentence of a natural language.<br>
 * It consists of a main word and a compound. The main word is the word which
 * carries the meaning while the compound is a less-meaningful addition to it.
 * The compound is often an article, adverb or adjective.
 *
 */
public class SentenceObject {

	private Word mMain;
	private Word mCompound;

	/**
	 * The constructor.
	 * 
	 * @param main
	 *            The main word. Must not be {@code null}.
	 * @param compound
	 *            The compound word. May be {@code null}.
	 */
	public SentenceObject(Word main, Word compound) {
		mMain = main;
		mCompound = compound;
	}

	/**
	 * Returns the main word.
	 * 
	 * @return The main {@link Word}.
	 */
	public Word getMainWord() {
		return mMain;
	}

	/**
	 * Returns the compound word.
	 * 
	 * @return The compound {@link Word}. May be {@code null}.
	 */
	public Word getCompound() {
		return mCompound;
	}
}