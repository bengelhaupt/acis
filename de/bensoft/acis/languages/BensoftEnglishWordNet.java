/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.languages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.bensoft.acis.core.language.Language;
import de.bensoft.acis.core.language.Sentence;
import de.bensoft.acis.core.language.SentenceObject;
import de.bensoft.acis.core.language.SentenceObjectSet;
import de.bensoft.acis.core.language.Word;
import de.bensoft.acis.core.language.Word.WordType;
import de.bensoft.acis.core.language.WordCache;
import de.bensoft.acis.utils.Logging.Loggable;
import de.bensoft.acis.utils.SimpleHTTPGetRequestSender;

/**
 * This is a Language for the ACIS system representing English. See the
 * {@link de.bensoft.acis.core.language.Language} interface for more information
 * on creating own Languages.<br>
 * <br>
 *
 * This language interface is based on the WordNet API of Princeton
 * University.<br>
 * <a href="http://wordnet.princeton.edu/">More information here</a>
 */
public class BensoftEnglishWordNet extends Loggable implements Language {

	private static final String LOG_TAG = "LANGUAGE_BENSOFT-ENGLISH";

	private WordCache mCache;

	@Override
	public String getName() {
		return "de.bensoft.acis.languages-english.wordnet";
	}

	@Override
	public Locale getLanguage() {
		return Locale.ENGLISH;
	}

	@Override
	public Word getWord(String word) {
		Word w = new Word(word, word.toLowerCase(), WordType.NotFound, new String[0]);
		try {
			w = mCache.readFromCache(word);
			w.setTypedForm(word);
			return w;
		} catch (IllegalArgumentException e) {
			try {
				String request = SimpleHTTPGetRequestSender.downloadData("http://wordnetweb.princeton.edu/perl/webwn?s="
						+ word.toLowerCase() + "&sub=Search+WordNet&o2=&o0=&o8=1&o1=&o7=&o5=&o9=&o6=&o3=&o4=");

				if (request.indexOf("</h3>\n</body>") == -1) {
					int h3 = request.indexOf("<div class=\"key\">");
					List<Integer> types = new ArrayList<>(0);
					while ((h3 = request.indexOf("<h3>", h3)) != -1) {
						String wordType = request.substring(h3 + 4, request.indexOf("</h3>", h3));
						h3++;
						if (wordType.startsWith("Noun"))
							types.add(EnglishWordType.Noun);
						else if (wordType.startsWith("Verb"))
							types.add(EnglishWordType.Verb);
						else if (wordType.startsWith("Adjective"))
							types.add(EnglishWordType.Adjective);
						else if (wordType.startsWith("Adverb"))
							types.add(EnglishWordType.Adverb);
					}
					if (types.size() == 0)
						types.add(EnglishWordType.Other);

					int type = types.get(0);

					String normalForm = word.toLowerCase();
					h3 = request.indexOf("<div class=\"key\">");
					if (request.toLowerCase().indexOf("<b>" + word.toLowerCase() + "</b>", h3) == -1) {
						normalForm = request.substring(h3).split("<li>.+?(<a href.+?\">)")[1].split("</a>")[0];
						request = SimpleHTTPGetRequestSender
								.downloadData("http://wordnetweb.princeton.edu/perl/webwn?s=" + normalForm
										+ "&sub=Search+WordNet&o2=&o0=&o8=1&o1=&o7=&o5=&o9=&o6=&o3=&o4=");
					}

					h3 = request.indexOf("<div class=\"key\">");
					List<String> synonyms = new ArrayList<>(0);
					while ((h3 = request.indexOf("<h3>", h3)) != -1) {
						String content = request.substring(request.indexOf("</h3>", h3), request.indexOf("</ul>", h3));
						int index = 0;
						while ((index = content.indexOf(";s=", index)) != -1) {
							int end = content.indexOf("\">", index);
							String p = content.substring(index + 3, end);
							if (p.equals(content.substring(end + 2, content.indexOf("<", end + 2)).replace("<b>", "")
									.replace("</b>", "")))
								addIfAbsent(synonyms, p);
							index++;
						}
						h3++;
					}

					getLogger().i(LOG_TAG, "Fetching Word data of '" + word + "' was successful.");
					w = new Word(word, normalForm, type, synonyms.toArray(new String[0]));
				} else {
					getLogger().w(LOG_TAG, "Word '" + word + "' was not found.");
				}
			} catch (Exception parseException) {
				getLogger().e(LOG_TAG, String.format("An error occured while parsing Word data of '%1$s' : %2$s", word,
						parseException.toString()));
			}
		}
		mCache.writeInCache(w);
		return w;
	}

	private boolean addIfAbsent(List<String> list, String item) {
		if (!list.contains(item) && item.trim() != "" && item != null) {
			list.add(item);
			return true;
		}
		return false;
	}

	private static class EnglishWordType {
		public static final int Noun = 1;
		public static final int Verb = 2;
		public static final int Adjective = 3;
		public static final int Adverb = 4;
		public static final int Other = 5;
	}

	@Override
	public Sentence getSentence(String sentence) {
		getLogger().i(LOG_TAG, "Analyzing Sentence '" + sentence + "'");
		Sentence s = new Sentence(sentence);

		// defining the sentence type based on it's ending character
		Sentence.SentenceType type;
		if (s.getText().trim().endsWith("?"))
			type = Sentence.SentenceType.Interrogative;
		else if (s.getText().trim().endsWith("!"))
			type = Sentence.SentenceType.Imperative;
		else
			type = Sentence.SentenceType.Statement;

		// eliminating some characters from the text and creating the word list
		String[] wordlist = s.getTypedWords();

		// analyzing the words and setting the prop
		Word[] analyzedWords = new Word[wordlist.length];
		for (int i = 0; i < analyzedWords.length; i++) {
			String word = wordlist[i];
			analyzedWords[i] = getWord(word);
		}

		s.setType(type);
		s.setWords(analyzedWords);

		return s;
	}

	@Override
	public SentenceObjectSet getSentenceObjects(Sentence sentence) {
		ArrayList<SentenceObject> actions = new ArrayList<SentenceObject>(0);
		ArrayList<SentenceObject> whats = new ArrayList<SentenceObject>(0);
		ArrayList<SentenceObject> whos = new ArrayList<SentenceObject>(0);
		ArrayList<SentenceObject> hows = new ArrayList<SentenceObject>(0);
		ArrayList<SentenceObject> elses = new ArrayList<SentenceObject>(0);

		SynthesizeWord[] words = new SynthesizeWord[sentence.getWords().length];
		for (int i = 0; i < words.length; i++)
			words[i] = toSynthesizeWord(sentence.getWords()[i]);

		for (int position = 0; position < words.length; position++) {
			if (words[position].getType() == EnglishWordType.Noun) {
				if (position > 0) {
					if (words[position - 1].getType() == EnglishWordType.Adjective
							|| words[position - 1].getType() == EnglishWordType.Adverb) {
						whats.add(new SentenceObject(words[position], words[position - 1]));
						words[position].setSynthesized(true);
						words[position - 1].setSynthesized(true);
					} else {
						whats.add(new SentenceObject(words[position], null));
						words[position].setSynthesized(true);
					}
				} else {
					whos.add(new SentenceObject(words[position], null));
					words[position].setSynthesized(true);
				}
			}
		}

		for (int position = 0; position < words.length; position++) {
			if (!(words[position].getType() != EnglishWordType.Adverb
					&& words[position].getType() != EnglishWordType.Adjective || words[position].isSynthesized())) {
				hows.add(new SentenceObject(words[position], null));
				words[position].setSynthesized(true);
			}
			if (!(words[position].getType() != EnglishWordType.Verb || words[position].isSynthesized())) {
				hows.add(new SentenceObject(words[position], null));
				words[position].setSynthesized(true);
			}
			if (!(words[position].getType() != EnglishWordType.Other || words[position].isSynthesized())) {
				elses.add(new SentenceObject(words[position], null));
				words[position].setSynthesized(true);
			}
		}

		return new SentenceObjectSet(actions.toArray(new SentenceObject[actions.size()]),
				whats.toArray(new SentenceObject[whats.size()]), whos.toArray(new SentenceObject[whos.size()]),
				hows.toArray(new SentenceObject[hows.size()]), elses.toArray(new SentenceObject[elses.size()]));
	}

	private SynthesizeWord toSynthesizeWord(Word word) {
		return new SynthesizeWord(word.getTypedForm(), word.getNormalForm(), word.getType(), word.getSynonyms());
	}

	private class SynthesizeWord extends Word {

		private boolean mIsSynthesized = false;

		public SynthesizeWord(String typedForm, String normalForm, int type, String[] synonyms) {
			super(typedForm, normalForm, type, synonyms);
		}

		public boolean isSynthesized() {
			return mIsSynthesized;
		}

		public void setSynthesized(boolean isSynthesized) {
			mIsSynthesized = isSynthesized;
		}
	}

	@Override
	public void setWordCache(WordCache wordCache) {
		mCache = wordCache;
	}

}
