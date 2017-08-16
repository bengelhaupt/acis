/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core.language;

import java.util.Locale;

import de.bensoft.acis.core.ACIS;

/**
 * The Language interface is the language-dependent part of an ACIS system. A
 * Language represents a unified interface the ACIS system can get information
 * from. Such information is e.g. a {@link Word} or a {@link SentenceObjectSet}.
 * <br>
 * A Language interface must not be abstract! <br>
 * See the example on <a
 * href="http://bensoft.de/projects/acis>http://bensoft.de/projects/acis</a> on
 * how to create your own Language interface.
 *
 */
public interface Language {

	/**
	 * Returns the name of the Language interface.
	 * 
	 * @return The name in a format similar to this:
	 *         "com.example.acis.languages-english". It must not contain
	 *         characters found in paths like "/\?:" etc.
	 */
	public String getName();

	/**
	 * Returns a {@link Locale} representing the language the interface is
	 * applicable for.
	 * 
	 * @return The language {@link Locale}.
	 */
	public Locale getLanguage();

	/**
	 * Looks up information about a specific word and returns a {@link Word}
	 * Object. <br>
	 * This function should first check whether the word is in the
	 * {@link WordCache} (if present) and eventually load information form
	 * there. <br>
	 * This function must provide:
	 * <ul>
	 * <li>The form the word was typed in</li>
	 * <li>The normal/base form</li>
	 * <li>The word type. Own word types are allowed as they are
	 * language-dependent.</li>
	 * <li>Synonyms of the word.</li>
	 * <li>The normal form (infinitive)</li>
	 * </ul>
	 * See {@link Word} for further information.
	 * <p>
	 * In case no information could be fetched about a word: The word type
	 * should be {@link Word.WordType#NotFound}, the NormalForm is the TypedForm
	 * and the synonyms are an empty array (not null).
	 * </p>
	 * 
	 * @param word
	 *            The input String word. May not contain whitespaces.
	 * @return A Word Object containing the information about this specific
	 *         word. Must not return {@code null}.
	 */
	public Word getWord(String word);

	/**
	 * Looks up information about a specific sentence (a row of words) and
	 * returns a {@link Sentence} Object. <br>
	 * This function must provide:
	 * <ul>
	 * <li>The type of the sentence. See {@link Sentence.SentenceType}.</li>
	 * <li>An array of {@link Word} Objects representing all Words of the
	 * input.</li>
	 * </ul>
	 * See {@link Sentence} for further information.
	 * 
	 * @param sentence
	 *            The input word sequence.
	 * @return The Sentence Object. Must not return {@code null}.
	 */
	public Sentence getSentence(String sentence);

	/**
	 * Returns a {@link SentenceObjectSet} containing the
	 * {@link SentenceObject}s.
	 * <p>
	 * This function must categorize ALL words into the categories ACTION, WHAT,
	 * WHO, HOW and ELSE (as represented by a {@link SentenceObject}).
	 * </p>
	 * <br>
	 * These categories are described as follows:
	 * <ul>
	 * <li>Action: The word(s) or group(s) of word(s) that describe the
	 * underlying action of an event. Usually verbs.</li>
	 * <li>What: The word(s) or group(s) of word(s) that represent a passive
	 * Object an action is performed on.</li>
	 * <li>Who: The word(s) or group(s) of word(s) that represent the
	 * action-taking subject.</li>
	 * <li>How: The word(s) or group(s) of word(s) that describe a way or state
	 * an action is performed in.</li>
	 * <li>Else: The word(s) or group(s) of word(s) that could not be put in the
	 * categories above or are uncategorizable.</li>
	 * </ul>
	 * <br>
	 * Note: Of course it is not very easy to programmatically categorize the
	 * parts of a sentence in natural language. Small deviations in
	 * interpretation are negligible. <br>
	 * See {@link SentenceObjectSet} for further information.
	 * 
	 * @param sentence
	 *            The input {@link Sentence} Object.
	 * @return A SentenceObjectSet containing the ACTIONS, WHATS, WHOS, HOWS and
	 *         ELSES as arrays of {@link SentenceObject}s.
	 */
	public SentenceObjectSet getSentenceObjects(Sentence sentence);

	/**
	 * Sets a {@link WordCache} Object for caching word information of this
	 * language. <br>
	 * A WordCache does not need to be set but is recommended because of
	 * performance (e.g. loading from the Internet). <br>
	 * Usually a WordCache is retrieved through {@link ACIS#getWordCache()}.
	 * 
	 * @param wordCache
	 *            The {@link WordCache} to use.
	 */
	void setWordCache(WordCache wordCache);
}