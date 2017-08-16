/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.languages;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.bensoft.acis.utils.Logging.Loggable;
import de.bensoft.acis.core.language.Language;
import de.bensoft.acis.core.language.Sentence;
import de.bensoft.acis.core.language.SentenceObject;
import de.bensoft.acis.core.language.SentenceObjectSet;
import de.bensoft.acis.core.language.Word;
import de.bensoft.acis.core.language.WordCache;
import de.bensoft.acis.core.language.Word.WordType;
import de.bensoft.acis.utils.SimpleHTTPGetRequestSender;

/**
 * This is a Language for the ACIS system representing German. See the
 * {@link de.bensoft.acis.core.language.Language} interface for more information
 * on creating own Languages.
 *
 * This should be taken as an example for a Language implementation.
 */
public class BensoftGermanWiktionary extends Loggable implements Language {

	private static final String LOG_TAG = "LANGUAGE_BENSOFT-GERMAN";

	private WordCache mCache;

	public Sentence getSentence(String inputtext) {
		getLogger().i(LOG_TAG, "Starting to analyze Sentence '" + inputtext + "'");

		Sentence s = new Sentence(inputtext);

		// defining the sentence type based on it's ending character
		Sentence.SentenceType type;
		if (s.getText().trim().endsWith("?")) {
			type = Sentence.SentenceType.Interrogative;
		} else {
			if (s.getText().trim().endsWith("!")) {
				type = Sentence.SentenceType.Imperative;
			} else {
				type = Sentence.SentenceType.Statement;
			}
		}

		// eliminating some characters from the text and creating the word list
		String[] wordlist = s.getTypedWords();

		// analyzing the words and setting the prop
		Word[] analyzedWords = new Word[wordlist.length];
		for (int i = 0; i < analyzedWords.length; i++) {
			String word = wordlist[i];
			analyzedWords[i] = getWord(word);
		}

		// Post-Analysis
		if (analyzedWords.length > 0) {
			if ((analyzedWords[0].getType() == GermanWordType.FlectedVerb
					&& analyzedWords[0].getTypedForm().endsWith("st")
					|| analyzedWords[0].getType() == GermanWordType.Interrogative)) {
				type = Sentence.SentenceType.Interrogative;
			}

			for (int i = 0; i < analyzedWords.length; i++) {
				if (analyzedWords[i].getType() == GermanWordType.Verb) {
					if (i > 0) {
						if (analyzedWords[i - 1].getType() == GermanWordType.PersonalPronoun
								|| analyzedWords[i - 1].getType() == GermanWordType.OtherPronoun) {
							analyzedWords[i].setType(GermanWordType.FlectedVerb);
						}
					}
					if ((i + 1) < analyzedWords.length)
						if (type == Sentence.SentenceType.Interrogative
								&& (analyzedWords[i + 1].getType() == GermanWordType.OtherPronoun
										|| analyzedWords[i + 1].getType() == GermanWordType.PersonalPronoun)) {
							analyzedWords[i].setType(GermanWordType.FlectedVerb);
						}
				}
			}
		}
		//

		s.setType(type);
		s.setWords(analyzedWords);

		getLogger().i(LOG_TAG, "Sentence successfully analyzed");

		return s;
	}

	public SentenceObjectSet getSentenceObjects(Sentence sentence) {
		ArrayList<SentenceObject> actions = new ArrayList<SentenceObject>(0);
		ArrayList<SentenceObject> whats = new ArrayList<SentenceObject>(0);
		ArrayList<SentenceObject> whos = new ArrayList<SentenceObject>(0);
		ArrayList<SentenceObject> hows = new ArrayList<SentenceObject>(0);
		ArrayList<SentenceObject> elses = new ArrayList<SentenceObject>(0);

		SynthesizeWord[] words = new SynthesizeWord[sentence.getWords().length];
		for (int i = 0; i < words.length; i++)
			words[i] = toSynthesizeWord(sentence.getWords()[i]);

		ArrayList<SynthesizeWord> verbs = new ArrayList<SynthesizeWord>();

		for (int position = 0; position < words.length; position++) {
			if (words[position].getType() == GermanWordType.Noun || words[position].getType() == GermanWordType.Name
					|| words[position].getType() == GermanWordType.PersonalPronoun) {
				if (position > 0) {
					if (words[position - 1].getType() == GermanWordType.Article) {
						SentenceObject nounarticle = new SentenceObject(words[position], words[position - 1]);
						if (position < 2) {
							whos.add(nounarticle);
						} else {
							whats.add(nounarticle);
						}
						words[position].setSynthesized(true);
						words[position - 1].setSynthesized(true);
					} else if (words[position - 1].getType() == GermanWordType.Adjective
							|| words[position - 1].getType() == GermanWordType.Adverb) {
						whats.add(new SentenceObject(words[position], words[position - 1]));
						words[position].setSynthesized(true);
						words[position - 1].setSynthesized(true);
					} else if (words[position - 1].getType() == GermanWordType.OtherPronoun) {
						whats.add(new SentenceObject(words[position], words[position - 1]));
						words[position].setSynthesized(true);
						words[position - 1].setSynthesized(true);
					} else if (words[position].getType() == GermanWordType.Name
							|| words[position].getType() == GermanWordType.PersonalPronoun) {
						whos.add(new SentenceObject(words[position], null));
						words[position].setSynthesized(true);
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
			if (!(words[position].getType() != GermanWordType.PersonalPronoun || words[position].isSynthesized())) {
				whos.add(new SentenceObject(words[position], null));
				words[position].setSynthesized(true);
			}
			if (!(words[position].getType() != GermanWordType.OtherPronoun
					&& words[position].getType() != GermanWordType.Article || words[position].isSynthesized())) {
				whats.add(new SentenceObject(words[position], null));
				words[position].setSynthesized(true);
			}
			if (!(words[position].getType() != GermanWordType.Interrogative
					&& words[position].getType() != GermanWordType.Adverb
					&& words[position].getType() != GermanWordType.Adjective
					&& words[position].getType() != GermanWordType.Preposition
					&& words[position].getType() != GermanWordType.Name
					&& words[position].getType() != GermanWordType.Number || words[position].isSynthesized())) {
				hows.add(new SentenceObject(words[position], null));
				words[position].setSynthesized(true);
			}
			if (!(words[position].getType() != GermanWordType.Conjunction
					&& words[position].getType() != GermanWordType.Abbreviation
					&& words[position].getType() != GermanWordType.Other || words[position].isSynthesized())) {
				elses.add(new SentenceObject(words[position], null));
				words[position].setSynthesized(true);
			}
			if (!(words[position].getType() != GermanWordType.Verb
					&& words[position].getType() != GermanWordType.FlectedVerb || words[position].isSynthesized())) {
				verbs.add(words[position]);
				words[position].setSynthesized(true);
			}
		}

		int verbCount = verbs.size();
		if (verbCount == 1) {
			actions.add(new SentenceObject((Word) verbs.get(0), null));
			verbs.get(0).setSynthesized(true);
		} else {
			int i = 0;
			while (i < verbCount) {
				SentenceObject wr;
				if (verbs.get(i).getType() == GermanWordType.FlectedVerb) {
					wr = new SentenceObject((Word) verbs.get(i), null);
					verbs.get(i).setSynthesized(true);
					actions.add(wr);
				} else {
					wr = new SentenceObject((Word) verbs.get(i), null);
					verbs.get(i).setSynthesized(true);
					hows.add(wr);
				}
				i++;
			}
		}

		getLogger().i(LOG_TAG, "Getting the SentenceObjects was successful");

		return new SentenceObjectSet(actions.toArray(new SentenceObject[actions.size()]),
				whats.toArray(new SentenceObject[whats.size()]), whos.toArray(new SentenceObject[whos.size()]),
				hows.toArray(new SentenceObject[hows.size()]), elses.toArray(new SentenceObject[elses.size()]));
	}

	public Word getWord(String word) {
		try {
			if (mCache.isInCache(word)) {
				Word w = mCache.readFromCache(word);
				w.setTypedForm(word);
				return w;
			} else {
				try {
					// trying getting the word info and setting it's properties
					Word w = WiktionaryWordInfoAPI.getWord(word);
					String normal = w.getNormalForm();
					int type = w.getType();
					String[] synonyms = w.getSynonyms();
					getLogger().i(LOG_TAG, "Fetching Word data of '" + word + "' was successful.");
					Word fword = new Word(word, normal, type, synonyms);
					mCache.writeInCache(fword);
					return fword;
				} catch (Exception ex) {
					getLogger().i(LOG_TAG, String.format(
							"An error occured while fetching the Word data of '%1$s' : %2$s", word, ex.toString()));
				}
			}
		} catch (Exception ex) {
			getLogger().i(LOG_TAG, String.format(
					"An error occured while trying to read the Word '%1$s' from the cache: %2$s", word, ex.toString()));
		}

		return null;
	}

	public final class GermanWordType extends WordType {
		public static final int Noun = 0;
		public static final int PersonalPronoun = 1;
		public static final int OtherPronoun = 2;
		public static final int Name = 3;
		public static final int FlectedVerb = 10;
		public static final int Verb = 11;
		public static final int Adverb = 20;
		public static final int Adjective = 21;
		public static final int Article = 22;
		public static final int Interrogative = 23;
		public static final int Number = 30;
		public static final int Conjunction = 31;
		public static final int Preposition = 32;
		public static final int Abbreviation = 40;
		public static final int Other = 50;
	}

	public static class WiktionaryWordInfoAPI {

		private static class Utils {

			public static String ToUpperStart(String s) {
				return s.substring(0, 1).toUpperCase() + s.substring(1);
			}

			public static String ToLowerStart(String s) {
				return s.substring(0, 1).toLowerCase() + s.substring(1);
			}

			public static String between(String text, String start, String end, int offset) {
				int typestart = text.indexOf(start, offset) + start.length();
				int typeend = text.indexOf(end, typestart);

				return text.substring(typestart, typeend);
			}

			public static boolean stringArrayContains(String[] array, String c) {
				for (int i = 0; i < array.length; i++) {
					if (array[i].equals(c))
						return true;
				}
				return false;
			}

			private static int[] allIndexesOf(String str, String value) {
				if (value == null || value.equals(""))
					throw new IllegalArgumentException("The String to find may not be empty: " + value);
				ArrayList<Integer> indexes = new ArrayList<>();
				int index = str.indexOf(value);
				while (index >= 0) {
					indexes.add(index);
					index = str.indexOf(value, index + 1);
				}
				Integer[] integers = indexes.toArray(new Integer[indexes.size()]);
				int[] ints = new int[integers.length];
				for (int i = 0; i < integers.length; i++) {
					ints[i] = integers[i];
				}
				return ints;
			}
		}

		private static int getBestSearchResult(String word) throws Exception {
			String searchuri = "https://de.wiktionary.com/w/api.php?action=query&format=xml&titles=";
			String or = "|";

			try {
				String requesturi = searchuri
						+ URLEncoder.encode(Utils.ToLowerStart(word) + or + Utils.ToUpperStart(word), "utf-8");
				String html = SimpleHTTPGetRequestSender.downloadData(requesturi);
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();

					Document doc = builder.parse(new ByteArrayInputStream(html.getBytes("UTF-8")));
					NodeList pages = doc.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(0)
							.getChildNodes();

					ArrayList<Integer> pageids = new ArrayList<>();
					ArrayList<String> pagenames = new ArrayList<>();
					for (int i = 0; i < pages.getLength(); i++) {
						if (pages.item(i).getAttributes().getNamedItem("missing") == null) {
							if (pages.item(i).getAttributes().getNamedItem("pageid") != null) {
								pageids.add(Integer
										.parseInt(pages.item(i).getAttributes().getNamedItem("pageid").getNodeValue()));
								pagenames.add(pages.item(i).getAttributes().getNamedItem("title").getNodeValue());
							}
						}
					}

					if (pageids.size() == 1) {
						return pageids.get(0);
					} else {
						if (pageids.size() == 0) {
							return -1;
						} else {
							for (int i = 0; i < pageids.size(); i++) {
								String title = pagenames.get(i);
								if (title.equals(word)) {
									return pageids.get(i);
								}
							}

							for (int i = 0; i < pageids.size(); i++) {
								String title = pagenames.get(i);
								if (Utils.ToUpperStart(title).equals(word) || Utils.ToLowerStart(title).equals(word)) {
									return pageids.get(i);
								}
							}

							return pageids.get(0);
						}
					}
				} catch (Exception ex) {
					throw new Exception(ex.toString());
				}
			} catch (Exception ex) {
				throw new Exception(ex.toString());
			}
		}

		private static String[] getSynonymes(String word) throws Exception {
			int pageid = getBestSearchResult(word);
			String parseuri = "https://de.wiktionary.com/w/api.php?action=parse&format=xml&prop=text&pageid=";

			String requesturi = parseuri + String.valueOf(pageid);

			String html = SimpleHTTPGetRequestSender.downloadData(requesturi);
			String start1 = "] &lt;a href=&quot;/wiki";
			String start2 = "title=&quot;";
			String end = "&quot;&gt;";

			String area = Utils.between(html, "&quot;&gt;Synonyme:&lt;/p&gt;\n&lt;dl&gt;\n&lt;dd&gt;",
					"&lt;/dd&gt;\n&lt;/dl&gt;", 0);
			int[] itempos = Utils.allIndexesOf(area, start1);
			ArrayList<String> synonymes = new ArrayList<String>();
			for (int pos : itempos) {
				String item = Utils.between(area, start2, end, pos);
				synonymes.add(item);
			}

			String[] strings = synonymes.toArray(new String[synonymes.size()]);
			String[] strs = new String[strings.length];
			for (int i = 0; i < strings.length; i++) {
				strs[i] = strings[i];
			}
			return strs;
		}

		public static Word getWord(String word) throws Exception {
			int type = WordType.NotFound;
			String[] synonymes = new String[0];
			String normalform = word;

			int pageid = getBestSearchResult(word);
			foreignword: if (pageid != -1) {
				String parseuri = "https://de.wiktionary.com/w/api.php?action=parse&format=xml&prop=text&pageid=";

				String requesturi = parseuri + String.valueOf(pageid);

				String requestresult = SimpleHTTPGetRequestSender.downloadData(requesturi);

				String language = Utils.between(requestresult, "(&lt;a href=&quot;/wiki/Wiktionary:",
						"&quot; title=&quot;Wiktionary:", 0);
				if (!language.equals("Deutsch")) {
					pageid = -1;
					break foreignword;
				}

				String wtype = Utils.between(requestresult,
						"&lt;a href=&quot;/wiki/Hilfe:Wortart&quot; title=&quot;Hilfe:Wortart&quot;&gt;", "&lt;/a&gt;",
						0);
				String additionaltype = null;
				try {
					additionaltype = Utils.between(requestresult,
							"&lt;a href=&quot;/wiki/Hilfe:Wortart&quot; title=&quot;Hilfe:Wortart&quot;&gt;",
							"&lt;/a&gt;",
							requestresult.indexOf(
									"&lt;a href=&quot;/wiki/Hilfe:Wortart&quot; title=&quot;Hilfe:Wortart&quot;&gt;")
									+ 1);
				} catch (Exception ex) {
					throw new Exception("WiktionaryAPI: Error while parsing content", ex);
				}

				switch (wtype) {
				case "Konjugierte Form":
				case "Partizip II":
					type = GermanWordType.FlectedVerb;
					String normalv = Utils.between(requestresult, "title=&quot;", "&quot;&gt;", requestresult.indexOf(
							"&gt;", requestresult.indexOf("&lt;/b&gt; ist eine flektierte Form von &lt;b&gt;")));
					normalform = normalv;
					break;
				case "Verb":
				case "Vollverb":
				case "Hilfsverb":
					type = GermanWordType.Verb;
					normalform = word;
					break;
				case "Adverb":
				case "Temporaladverb":
				case "Pronominaladverb":
				case "Konjunktionaladverb":
					type = GermanWordType.Adverb;
					String[] interrogativewords = new String[] { "wer", "was", "wie", "wo", "wohin", "woher", "wann",
							"wieso", "weshalb", "warum", "wozu" };
					if (Utils.stringArrayContains(interrogativewords, word)) {
						type = GermanWordType.Interrogative;
					}
					normalform = word;
					break;
				case "Adjektiv":
					type = GermanWordType.Adjective;
					normalform = word;
					break;
				case "Abkürzung":
					type = GermanWordType.Abbreviation;
					normalform = word;
					break;
				case "Artikel":
					type = GermanWordType.Article;
					normalform = word;
					break;
				case "Konjunktion":
					type = GermanWordType.Conjunction;
					normalform = word;
					break;
				case "Numerale":
				case "Zahlzeichen":
					type = GermanWordType.Number;
					normalform = word;
					break;
				case "Pronomen":
				case "Personalpronomen":
					type = GermanWordType.PersonalPronoun;
					normalform = word;
					break;
				case "Indefinitpronomen":
				case "Reflexivpronomen":
				case "Possessivpronomen":
				case "Demonstrativpronomen":
					type = GermanWordType.OtherPronoun;
					normalform = word;
					break;
				case "Substantiv":
					if (additionaltype != null) {
						if (additionaltype == "Vorname" || additionaltype == "Nachname") {
							type = GermanWordType.Name;
						} else {
							type = GermanWordType.Noun;
						}
					} else {
						type = GermanWordType.Noun;
					}
					normalform = word;
					break;
				case "Eigenname":
					type = GermanWordType.Name;
					normalform = word;
					break;
				case "Adposition":
				case "Präposition":
				case "Kontraktion":
					type = GermanWordType.Preposition;
					normalform = word;
					break;
				case "Deklinierte Form":
					try {
						String start = "&lt;/b&gt; ist eine flektierte Form von &lt;b&gt;";
						String normals = Utils.between(requestresult, "&gt;", "&lt;",
								requestresult.indexOf("&gt;", requestresult.indexOf(start) + start.length()));
						if (normals.equals(word)) {
							throw new Exception("Normal form of word is old form");
						}
						normalform = normals;
						type = getWord(normals).getType();
					} catch (Exception ignored) {
						normalform = word;
						type = GermanWordType.Other;
					}
					break;
				default:
					type = GermanWordType.Other;
					normalform = word;
					break;
				}

				synonymes = getSynonymes(normalform);
			} else {
				try {
					normalform = String.valueOf(Integer.parseInt(word));
					type = GermanWordType.Number;
					synonymes = new String[0];
				} catch (Exception ignored) {
					normalform = word;
					type = WordType.NotFound;
					synonymes = new String[0];
				}
			}

			return new Word(word, normalform, type, synonymes);
		}
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
	public String getName() {
		return "de.bensoft.acis.languages-german.wiktionary";
	}

	@Override
	public void setWordCache(WordCache wordCache) {
		mCache = wordCache;
	}

	@Override
	public Locale getLanguage() {
		return Locale.GERMAN;
	}
}