/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.bensoft.acis.core.Action;

/**
 * Represents a sentence (a row of words) and its properties such as type.
 *
 */
public class Sentence {

	/**
	 * Specifies the characters that are filtered from the input and replaced
	 * with "". This does not include sentence separators like '.' or '!'. <br>
	 * Another approach would have been to specify the allowed characters, but
	 * this seemed too difficult because there are too many alphabets in the
	 * world.
	 */
	public final static char[] mForbiddenCharacters = "!\"§%&/()=?²³{[]}\\*+~#|°^".toCharArray();

	public final static char[] mSentenceDelimiters = " .,;:".toCharArray();

	private final static String mSentenceDelimiterRegex = "\\s+|\\.|,|;|:";
	private final static String mParameterRegex = "<<.+>>";
	private final static char[] mParameterCharacters = "<$>".toCharArray();

	private String mText;
	private String mTextWithoutParams;
	private SentenceType mType;
	private String[] mParts;
	private String[] mTypedWords;
	private Word[] mWords;

	/**
	 * The constructor.
	 * 
	 * @param inputText
	 *            The text from the user input.
	 */
	public Sentence(String inputText) {
		mText = inputText;
		mTextWithoutParams = eliminateParameters(mText);
		mParts = splitUp(mText);
		mTypedWords = splitUp(mTextWithoutParams);
	}

	/**
	 * Returns the type of the sentence.
	 * 
	 * @return A {@link Sentence.SentenceType} representing the type.
	 */
	public SentenceType getType() {
		return mType;
	}

	/**
	 * Sets the type of the sentence.
	 * 
	 * @param type
	 *            A {@link Sentence.SentenceType} to set as the type of the
	 *            sentence.
	 */
	public void setType(SentenceType type) {
		mType = type;
	}

	/**
	 * Returns the {@link Word}s of the Sentence.
	 * 
	 * @return The {@link Word} array.
	 */
	public Word[] getWords() {
		return mWords;
	}

	/**
	 * Sets the {@link Word}s of the Sentence.
	 * 
	 * @param words
	 *            The {@link Word} array to set.
	 */
	public void setWords(Word[] words) {
		mWords = words;
	}

	/**
	 * Returns the whole input text including any parameter declarations.
	 * 
	 * @return The input text String.
	 */
	public String getText() {
		return mText;
	}

	/**
	 * Returns the input text with parameter declarations removed from it.
	 * 
	 * @return The input text String without parameters.
	 */
	public String getTextWithoutParams() {
		return mTextWithoutParams;
	}

	/**
	 * Returns the input text split up at sentence separators.<br>
	 * See {@link mSentenceDelimiters}.
	 * 
	 * @return An array containing all parts of the sentence. This also includes
	 *         any parameter declarations.
	 */
	public String[] getParts() {
		return mParts;
	}

	/**
	 * Returns the input text without parameters split up at sentence
	 * separators.<br>
	 * See {@link mSentenceDelimiters}.
	 * 
	 * @return An array containing the parts of the sentence, not including
	 *         parameter declarations.
	 */
	public String[] getTypedWords() {
		return mTypedWords;
	}

	/**
	 * Returns the first occurrence of a word in the sentence.<br>
	 * Note: This function searches the same array as {@link #getParts()}
	 * returns. This means it can deliver parameter declarations too.
	 * 
	 * @param word
	 *            The word String.
	 * @param offset
	 *            A number of sentence parts after which the search starts. Must
	 *            be 0 or bigger.
	 * @return The first index of {@code word} in the {@link #getParts()} array
	 *         after the specified offset. Returns {@code -1} if not found.
	 */
	public int indexOfWord(String word, int offset) {
		for (int i = offset; i < mParts.length; i++) {
			if (mParts[i].equals(word))
				return i;
		}
		return -1;
	}

	/**
	 * Eliminates all parameter declarations from a String.
	 * 
	 * @param input
	 *            The input.
	 * @return The {@code input} String without any parameter declarations.
	 */
	private static String eliminateParameters(String input) {
		return input.replaceAll(mParameterRegex, "").replaceAll("  ", " ");
	}

	/**
	 * Splits up a sentence String at the {@link mSentenceDelimiters} and
	 * filters out {@link mForbiddenCharacters}.<br>
	 * Note: This will also filter out parameter declarations.
	 * 
	 * @param sentence
	 *            The input sentence.
	 * @return A array containing the split up parts.
	 */
	private static String[] splitUp(String sentence) {
		for (char s : sentence.toCharArray()) {
			if ((charArrayContains(mForbiddenCharacters, s) || charArrayContains(mParameterCharacters, s))
					&& !charArrayContains(mSentenceDelimiters, s)) {
				sentence = sentence.replace(Character.toString(s), "");
			}
		}
		List<String> list = new ArrayList<>(0);
		list.addAll(Arrays.asList(sentence.trim().split(mSentenceDelimiterRegex)));
		list.removeAll(Arrays.asList("", null));
		return list.toArray(new String[0]);
	}

	/**
	 * This function is used to split up an {@link Action} trigger String.
	 * 
	 * @param trigger
	 *            The {@link Action}'s trigger String.
	 * @return A array containing all split up parts of the trigger including
	 *         parameter declarations.
	 */
	public static String[] splitUpAction(String trigger) {
		for (char s : trigger.toCharArray()) {
			if (charArrayContains(mForbiddenCharacters, s) && !charArrayContains(mSentenceDelimiters, s)) {
				trigger = trigger.replace(Character.toString(s), "");
			}
		}
		trigger.replaceAll("  ", " ");
		return trigger.trim().split(mSentenceDelimiterRegex);
	}

	/**
	 * Checks whether a char is in a char array.
	 * 
	 * @param array
	 *            The array to search the char in.
	 * @param c
	 *            The char to search for.
	 * @return {@code true} when {@code c} is contained in the {@code array},
	 *         else {@code false}.
	 */
	private static boolean charArrayContains(char[] array, char c) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == c)
				return true;
		}
		return false;
	}

	/**
	 * This class represents the different types of sentences.<br>
	 * They are described as follows:<br>
	 * <ul>
	 * <li>Interrogative: Questions</li>
	 * <li>Imperative: Orders</li>
	 * <li>Statement: General expressions and everything which is none of the
	 * above</li>
	 * </ul>
	 *
	 */
	public enum SentenceType {
		Interrogative, Imperative, Statement
	}
}