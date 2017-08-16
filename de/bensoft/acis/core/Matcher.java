/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

import java.util.ArrayList;
import java.util.Comparator;

import de.bensoft.acis.core.language.Language;
import de.bensoft.acis.core.language.Sentence;
import de.bensoft.acis.core.language.SentenceObject;
import de.bensoft.acis.core.language.SentenceObjectSet;
import de.bensoft.acis.core.language.Word;
import de.bensoft.acis.utils.Logging.Loggable;

/**
 * The Matcher is a core class of the {@link ACIS} system. It is responsible for
 * matching (comparing) the {@link Action}s implemented in the system with the
 * user input. It compares the {@link Action}s on behalf of the length, words,
 * parameters and context. For that a score between 0 and 1 is calculated.
 * 
 */
class Matcher extends Loggable {

	private static final String LOG_TAG = "MATCHER";

	private Action[] mActions;
	private Language mLanguage;
	private float mParameterThreshold;

	/**
	 * Constructor of a Matcher.
	 * 
	 * @param language
	 *            The {@link Language} to use.
	 * @param actions
	 *            The {@link Action}s to compare the input with.
	 */
	public Matcher(Language language, Action[] actions, float parameterThreshold) {
		mActions = actions;
		mLanguage = language;
		mParameterThreshold = parameterThreshold;
	}

	/**
	 * A function which compares the {@link Action}s with the input and returns
	 * a {@link MatchResult} for every {@link Action} in the system of which the
	 * matching score is higher than {@code threshold}.
	 * 
	 * @param sentence
	 *            The user input {@link Sentence}.
	 * @param sentenceObjects
	 *            The {@link SentenceObjectSet} resulting from an analysis of
	 *            the input.
	 * @param threshold
	 *            The score threshold under which a {@link MatchResult} of an
	 *            {@link Action} is not contained in the returning array.
	 * @param weightset
	 *            The {@link Matcher.WeightSet} containing the weights used for
	 *            comparison.
	 * @return An array of {@link MatchResult}s with a matching score above the
	 *         {@code threshold}.
	 */
	public MatchResult[] getMatches(Sentence sentence, SentenceObjectSet sentenceObjects, ContextStack generalContext,
			float threshold, WeightSet weightset) {
		getLogger().i(LOG_TAG, "Matcher started for Sentence '" + sentence.getText() + "' with threshold "
				+ String.valueOf(threshold));

		ArrayList<MatchResult> results = new ArrayList<>();

		for (Action i : mActions) {
			float score = 0;
			float maxpossible = 0;

			// Matching of length
			maxpossible += sentence.getParts().length * weightset.Length;
			int diff = Math.abs(sentence.getParts().length - Sentence.splitUpAction(i.getTrigger()).length);
			if (diff <= sentence.getParts().length)
				score += (sentence.getParts().length - diff) * weightset.Length;

			// Matching of SentenceObjects
			SentenceObjectSet vs = mLanguage.getSentenceObjects(mLanguage.getSentence(i.getTrigger()));
			float curmaxpossible = 0f;
			if (sentenceObjects.getActions().length != 0 || vs.getActions().length != 0) {
				float[] result = Matcher.compareSentenceObjects(weightset, sentenceObjects.getActions(),
						vs.getActions());
				score += weightset.ActionMultiplier * result[0];
				curmaxpossible += weightset.ActionMultiplier * result[1];
			}
			if (sentenceObjects.getWhos().length != 0 || vs.getWhos().length != 0) {
				float[] result = Matcher.compareSentenceObjects(weightset, sentenceObjects.getWhos(), vs.getWhos());
				score += weightset.WhoMultiplier * result[0];
				curmaxpossible += weightset.WhoMultiplier * result[1];
			}
			if (sentenceObjects.getWhats().length != 0 || vs.getWhats().length != 0) {
				float[] result = Matcher.compareSentenceObjects(weightset, sentenceObjects.getWhats(), vs.getWhats());
				score += weightset.WhatMultiplier * result[0];
				curmaxpossible += weightset.WhatMultiplier * result[1];
			}
			if (sentenceObjects.getHows().length != 0 || vs.getHows().length != 0) {
				float[] result = Matcher.compareSentenceObjects(weightset, sentenceObjects.getHows(), vs.getHows());
				score += weightset.HowMultiplier * result[0];
				curmaxpossible += weightset.HowMultiplier * result[1];
			}
			if (sentenceObjects.getElses().length != 0 || vs.getElses().length != 0) {
				float[] result = Matcher.compareSentenceObjects(weightset, sentenceObjects.getElses(), vs.getElses());
				score += weightset.ElseMultiplier * result[0];
				curmaxpossible += weightset.ElseMultiplier * result[1];
			}

			if (curmaxpossible == 0) {
				maxpossible += 0.5 * weightset.ActionMultiplier
						* (sentenceObjects.getActions().length + vs.getActions().length)
						+ 0.5 * weightset.WhatMultiplier * (sentenceObjects.getWhats().length + vs.getWhats().length)
						+ 0.5 * weightset.WhoMultiplier * (sentenceObjects.getWhos().length + vs.getWhos().length)
						+ 0.5 * weightset.HowMultiplier * (sentenceObjects.getHows().length + vs.getHows().length)
						+ 0.5 * weightset.ElseMultiplier * (sentenceObjects.getElses().length + vs.getElses().length);
			} else {
				maxpossible += curmaxpossible;
			}

			ArrayList<Parameter> parameter = new ArrayList<Parameter>(0);
			if ((score / maxpossible) > mParameterThreshold) {
				// Matching of Parameters

				// exact matching by word strings
				String[] words = sentence.getParts();
				ActionParameter[] inputparams = i.getActionParams();
				for (int a = 0; a < inputparams.length; a++) {
					String currentparam = null;
					String predelimiter = inputparams[a].getPreDelimiter();
					String postdelimiter = inputparams[a].getPostDelimiter();

					// starting at beginning, searching for enddelimiter
					if (predelimiter == null) {
						if (sentence.indexOfWord(postdelimiter, 1) != -1) {
							String tmp = getFromBeginning(sentence.getText(), postdelimiter);
							if (!tmp.equals("")) {
								currentparam = tmp;
							}
						} else {
							Word postdelimiterWord = mLanguage.getWord(postdelimiter);
							for (int k = 0; k < words.length; k++) {
								if (sentence.getWords()[k].equalsSynonym(postdelimiterWord, false)) {
									String tmp = getFromBeginning(sentence.getText(), sentence.getParts()[k]);
									if (!tmp.equals("")) {
										currentparam = tmp;
										break;
									}
								}
							}
						}
					} else {
						// at the end, searching for startdelimiter
						if (postdelimiter == null) {
							Word predelimiterWord = mLanguage.getWord(predelimiter);
							if (sentence.indexOfWord(predelimiter, 0) != -1) {
								String tmp = getUntilEnd(sentence.getText(), predelimiter);
								if (!tmp.equals("")) {
									currentparam = tmp;
								}
							} else {
								for (int k = 0; k < words.length; k++) {
									if (sentence.getWords()[k].equalsSynonym(predelimiterWord, false)) {
										String tmp = getUntilEnd(sentence.getText(), sentence.getParts()[k]);
										if (!tmp.equals("")) {
											currentparam = tmp;
											break;
										}
									}
								}
							}
						} else {
							// somewhere else
							if (postdelimiter != null && predelimiter != null) {
								boolean scored = false;
								for (int b = 0; b < words.length; b++)
									if (words[b].equals(predelimiter)) {
										int pos = sentence.indexOfWord(postdelimiter, b + 1);
										if (pos != -1) {
											String tmp = getBetweenWords(sentence.getText(), words[b], words[pos]);
											if (!tmp.equals("")) {
												currentparam = tmp;
												scored = true;
											}
										}
									}
								if (!scored) {
									Word predelimiterWord = mLanguage.getWord(predelimiter);
									Word postdelimiterWord = mLanguage.getWord(postdelimiter);
									for (int b = 0; b < words.length; b++)
										if (sentence.getWords()[b].equalsSynonym(predelimiterWord, false)) {
											for (int k = b + 1; k < words.length; k++) {
												if (sentence.getWords()[k].equalsSynonym(postdelimiterWord, false)) {
													String tmp = getBetweenWords(sentence.getText(),
															sentence.getParts()[b], sentence.getParts()[k]);
													if (!tmp.equals("")) {
														currentparam = tmp;
														break;
													}
												}
											}
										}
								}
							}
						}
					}

					if (currentparam != null) {
						parameter.add(new Parameter(inputparams[a].getType(), inputparams[a].getIndex(), currentparam));
					}
				}

				maxpossible += inputparams.length * weightset.ParameterCount;
				if (inputparams.length != 0)
					score += inputparams.length * ((float) parameter.size() / inputparams.length)
							* weightset.ParameterCount;

				// matching context
				if (generalContext.getItems().length > 0) {
					float ageMultiplier = weightset.ContextStageOutdatedMultiplier;
					long age = generalContext.getItems()[0].getAge();
					if (age < 300000)
						ageMultiplier = weightset.ContextStageRecentMultiplier;
					if (age < 60000)
						ageMultiplier = weightset.ContextStageCurrentMultiplier;
					if (age < 10000)
						ageMultiplier = weightset.ContextStageImmediateMultiplier;

					Action action = generalContext.getItems()[0].getAction();

					// same context
					String prevContext = "";
					if (action instanceof ContextConstructorAction)
						prevContext = ((ContextConstructorAction) action).getContextId();
					if (action instanceof ContextDependentAction)
						prevContext = ((ContextDependentAction) action).getDependingContextId();

					String currentContext = "";
					if (i instanceof ContextConstructorAction)
						currentContext = ((ContextConstructorAction) i).getContextId();
					if (i instanceof ContextDependentAction)
						currentContext = ((ContextDependentAction) i).getDependingContextId();

					if (prevContext != "" && prevContext.equals(currentContext)) {
						score += ageMultiplier * weightset.ContextPreviousSameContext;
						maxpossible += ageMultiplier * weightset.ContextPreviousSameContext;
					} else {
						// same ActionPackage
						if (action.getPackage().equals(i.getPackage())) {
							score += ageMultiplier * weightset.ContextPreviousSameActionPackage;
							maxpossible += ageMultiplier * weightset.ContextPreviousSameActionPackage;
						}
					}
				}
			}

			score = score / maxpossible;
			if (score >= threshold) {
				parameter.sort(new Comparator<Parameter>() {

					@Override
					public int compare(Parameter o1, Parameter o2) {
						int a = o1.getIndex();
						int b = o2.getIndex();
						return a > b ? +1 : a < b ? -1 : 0;
					}

				});
				Parameter[] params = new Parameter[parameter.size()];
				params = parameter.toArray(params);
				results.add(new MatchResult(i, score, params));
			}
		}

		getLogger().i(LOG_TAG, "Matcher finished with " + String.valueOf(results.size()) + " results.");

		return results.toArray(new MatchResult[results.size()]);
	}

	/**
	 * Compares two {@link SentenceObject}s.
	 * 
	 * @param weights
	 *            The {@link WeightSet} to use.
	 * @param element
	 *            The first {@link SentenceObject}.
	 * @param target
	 *            The second {@link SentenceObject}.
	 * @return A float array of length 2: [0] is the reached score, [1] is the
	 *         maximum score.
	 */
	private static float[] compareSentenceObjects(WeightSet weights, SentenceObject[] element,
			SentenceObject[] target) {
		float totalscore = 0;
		float totalmaxpossible = 0;

		for (SentenceObject t : target) {
			float maxmaxpossible = 1;
			float maxscore = 0;

			for (SentenceObject e : element) {
				float maxpossible = 0;
				float score = 0;

				maxpossible += weights.MainWordMultiplier * weights.WordExact;
				if (t.getMainWord().equalsExact(e.getMainWord())) {
					score += weights.MainWordMultiplier * weights.WordExact;
				}
				maxpossible += weights.MainWordMultiplier * weights.WordSame;
				if (t.getMainWord().equalsNormalForm(e.getMainWord(), false)) {
					score += weights.MainWordMultiplier * weights.WordSame;
				}
				if (t.getMainWord().equalsSynonym(e.getMainWord(), false)) {
					score += weights.MainWordMultiplier * weights.WordSynonyme;
					maxpossible += weights.MainWordMultiplier * weights.WordSynonyme;
				}

				if (t.getCompound() != null && e.getCompound() != null) {
					maxpossible += weights.WordExact;
					if (t.getCompound().equalsExact(e.getCompound())) {
						score += weights.WordExact;
					}
					maxpossible += weights.WordSame;
					if (t.getCompound().equalsNormalForm(e.getCompound(), false)) {
						score += weights.WordSame;
					}
					if (t.getCompound().equalsSynonym(e.getCompound(), false)) {
						score += weights.WordSynonyme;
						maxpossible += weights.WordSynonyme;
					}
				}

				if (score / maxpossible > maxscore / maxmaxpossible) {
					maxscore = score;
					maxmaxpossible = maxpossible;
				}
			}
			totalscore += maxscore;
			totalmaxpossible += maxmaxpossible;
		}

		return new float[] { totalscore, totalmaxpossible };
	}

	/**
	 * Returns the content between two delimiter Strings in a String.
	 * 
	 * @param text
	 *            The input String.
	 * @param word1
	 *            The first delimiter. Must be contained in {@code text}.
	 * @param word2
	 *            The second delimiter. Must be contained in {@code text}.
	 * @return The part between {@code word1} and {@code word2}.
	 */
	private static String getBetweenWords(String text, String word1, String word2) {
		int startpos = text.indexOf(word1) + word1.length();
		int endpos = text.indexOf(word2);
		return text.substring(startpos, endpos).trim();
	}

	/**
	 * Returns the part of a String from the beginning until the first
	 * occurrence of another String.
	 * 
	 * @param text
	 *            The input String.
	 * @param endword
	 *            The String until which the text should be returned.
	 * @return The part of the input until {@code endword}.
	 */
	private static String getFromBeginning(String text, String endword) {
		int endpos = text.indexOf(endword);
		return text.substring(0, endpos).trim();
	}

	/**
	 * Returns the part of a String from the first occurrence of another String
	 * until the end.
	 * 
	 * @param text
	 *            The input String.
	 * @param startword
	 *            The String after which the text should be returned.
	 * @return The part of the input beginning at {@code startword}.
	 */
	private static String getUntilEnd(String text, String startword) {
		int startpos = text.indexOf(startword) + startword.length();
		return text.substring(startpos).trim();
	}
}