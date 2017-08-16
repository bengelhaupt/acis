/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core.language;

/**
 * Represents a word of a sentence including information such as the normal
 * form, type or synonyms.<br>
 * Note that the type is language-dependent and defined in the respective
 * {@link Language} interface.
 *
 */
public class Word {

	private String mTypedForm;
	private String mNormalForm;
	private int mType;
	private String[] mSynonyms;

	/**
	 * The constructor.
	 * 
	 * @param typedForm
	 *            The form the word occurs in the input.
	 * @param normalForm
	 *            The normal/base form of a word.
	 * @param type
	 *            The type.
	 * @param synonyms
	 *            An array of synonyms. If there are no synonyms, pass an empty
	 *            array. Must not be {@code null}.
	 */
	public Word(String typedForm, String normalForm, int type, String[] synonyms) {
		mTypedForm = typedForm;
		mNormalForm = normalForm;
		mType = type;
		mSynonyms = synonyms;
	}

	/**
	 * Returns the form the word occurred in the input.
	 * 
	 * @return The typed form of the word.
	 */
	public String getTypedForm() {
		return mTypedForm;
	}

	/**
	 * Sets the form the word occurred in the input.
	 * 
	 * @param typedForm
	 *            The typed form of the word.
	 */
	public void setTypedForm(String typedForm) {
		mTypedForm = typedForm;
	}

	/**
	 * Returns the normal/base form of a word.
	 * 
	 * @return The normal form.
	 */
	public String getNormalForm() {
		return mNormalForm;
	}

	/**
	 * Sets the normal/base form if a word.
	 * 
	 * @param normalForm
	 *            The normal form.
	 */
	public void setNormalForm(String normalForm) {
		mNormalForm = normalForm;
	}

	/**
	 * Returns the type of the word.
	 * 
	 * @return An {@code int} representing the type of the word.
	 */
	public int getType() {
		return mType;
	}

	/**
	 * Sets the type of the word.
	 * 
	 * @param type
	 *            An {@code int} representing the type of the word. Note: This
	 *            is language-dependent, so the word types defined in the
	 *            respective {@link Language} interface should be used.
	 */
	public void setType(int type) {
		mType = type;
	}

	/**
	 * Returns the synonyms of the word.
	 * 
	 * @return An array containing Strings of synonyms in normal form.
	 */
	public String[] getSynonyms() {
		return mSynonyms;
	}

	/**
	 * Sets the synonyms of the word.
	 * 
	 * @param synonyms
	 *            The synonyms array to set. Must not be {@code null}.
	 */
	public void setSynonyms(String[] synonyms) {
		mSynonyms = synonyms;
	}

	/**
	 * Contains the standard word types.
	 *
	 */
	public static class WordType {

		/**
		 * The type to assign when the word is unknown or no information could
		 * have been found about it.
		 */
		public static final int NotFound = -1;
	}

	/**
	 * Checks whether the word is the synonym of another or the other way
	 * around. The NormalForm of the word is used for checking.
	 * 
	 * @param word
	 *            The word to check with.
	 * @param caseSensitive
	 *            Whether the comparison should be case sensitive.
	 * @return {@code true} when they are synonyms, {@code false} when not.
	 */
	public boolean equalsSynonym(Word word, boolean caseSensitive) {
		if (stringArrayContains(word.getSynonyms(), mNormalForm, caseSensitive)
				|| stringArrayContains(mSynonyms, word.getNormalForm(), caseSensitive))
			return true;
		return false;
	}

	/**
	 * Checks whether a String is in a String array. This also checks for the
	 * lower case version of the String.
	 * 
	 * @param array
	 *            The array to search the String in.
	 * @param c
	 *            The String to search for.
	 * @param caseSensitive
	 *            Whether the comparison should be case sensitive.
	 * @return {@code true} when {@code c} is contained in the {@code array},
	 *         else {@code false}.
	 */
	private boolean stringArrayContains(String[] array, String c, boolean caseSensitive) {
		for (int i = 0; i < array.length; i++) {
			if ((caseSensitive && array[i].equals(c)) || !caseSensitive && array[i].equalsIgnoreCase(c))
				return true;
		}
		return false;
	}

	/**
	 * Checks whether the normal form of the word equals the normal form of
	 * another.
	 * 
	 * @param word
	 *            The word to check with.
	 * @param caseSensitive
	 *            Whether the comparison should be case sensitive.
	 * @return {@code true} when they have the same normal form, {@code false}
	 *         when not.
	 */
	public boolean equalsNormalForm(Word word, boolean caseSensitive) {
		return (caseSensitive && mNormalForm.equals(word.getNormalForm())
				|| !caseSensitive && mNormalForm.equalsIgnoreCase(word.getNormalForm()));
	}

	/**
	 * Checks whether the typed form of the word equals the typed form of
	 * another.
	 * 
	 * @param word
	 *            The word to check with.
	 * @return {@code true} when they have the same typed form, {@code false}
	 *         when not.
	 */
	public boolean equalsExact(Word word) {
		return mTypedForm.equals(word.getTypedForm());
	}

	/**
	 * Checks whether the word equals another in any way.
	 * 
	 * @param word
	 *            The word to check with.
	 * @param caseSensitive
	 *            Whether the comparison should be case sensitive.
	 * @return {@code true} when they have the same typed form, normal form or
	 *         share synonyms. {@code false} when not.
	 */
	public boolean equals(Word word, boolean caseSensitive) {
		return equalsExact(word) || equalsNormalForm(word, caseSensitive) || equalsSynonym(word, caseSensitive);
	}
}