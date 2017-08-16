/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.languages;

import java.util.ArrayList;
import java.util.Locale;

import de.bensoft.acis.core.language.Language;
import de.bensoft.acis.core.language.Sentence;
import de.bensoft.acis.core.language.SentenceObject;
import de.bensoft.acis.core.language.SentenceObjectSet;
import de.bensoft.acis.core.language.Word;
import de.bensoft.acis.core.language.WordCache;
import de.bensoft.acis.core.language.Word.WordType;

/**
 * A unified language interface applicable to all natural languages.<br>
 * Every word is categorized into the WHAT of the SentenceObjects.
 * 
 * Note: This has less accuracy than the high-quality language interfaces.
 *
 */
public class Unified implements Language {

	@Override
	public String getName() {
		return "de.bensoft.acis.languages-unified";
	}

	@Override
	public Locale getLanguage() {
		return Locale.ROOT;
	}

	@Override
	public Word getWord(String word) {
		return new Word(word, word, WordType.NotFound, new String[0]);
	}

	@Override
	public Sentence getSentence(String sentence) {
		Sentence sen = new Sentence(sentence);

		Sentence.SentenceType type;
		if (sen.getText().trim().endsWith("?")) {
			type = Sentence.SentenceType.Interrogative;
		} else {
			if (sen.getText().trim().endsWith("!")) {
				type = Sentence.SentenceType.Imperative;
			} else {
				type = Sentence.SentenceType.Statement;
			}
		}

		String[] wordList = sen.getTypedWords();

		Word[] analyzedWords = new Word[wordList.length];
		for (int i = 0; i < analyzedWords.length; i++) {
			String word = wordList[i];
			analyzedWords[i] = getWord(word);
		}

		sen.setType(type);
		sen.setWords(analyzedWords);

		return sen;
	}

	@Override
	public SentenceObjectSet getSentenceObjects(Sentence sentence) {
		ArrayList<SentenceObject> actions = new ArrayList<SentenceObject>(0);
		ArrayList<SentenceObject> whats = new ArrayList<SentenceObject>(0);
		ArrayList<SentenceObject> whos = new ArrayList<SentenceObject>(0);
		ArrayList<SentenceObject> hows = new ArrayList<SentenceObject>(0);
		ArrayList<SentenceObject> elses = new ArrayList<SentenceObject>(0);

		for (int i = 0; i < sentence.getWords().length; i++) {
			whats.add(new SentenceObject(sentence.getWords()[i], null));
		}

		return new SentenceObjectSet(actions.toArray(new SentenceObject[actions.size()]),
				whats.toArray(new SentenceObject[whats.size()]), whos.toArray(new SentenceObject[whos.size()]),
				hows.toArray(new SentenceObject[hows.size()]), elses.toArray(new SentenceObject[elses.size()]));
	}

	@Override
	public void setWordCache(WordCache wordCache) {
		return;
	}
}