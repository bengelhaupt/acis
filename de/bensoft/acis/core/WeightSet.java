/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

import de.bensoft.acis.core.language.Language;
import de.bensoft.acis.core.language.Sentence;
import de.bensoft.acis.core.language.SentenceObjectSet;
import de.bensoft.acis.core.language.Word;

/**
 * A class containing several values used as weights when comparing
 * {@link Action}s in the Matcher.
 * 
 * First the length is compared, then the words, parameters and lastly context.
 * 
 * Unless you know how the weights alter the score, the use of the default
 * values should be sufficient.
 * 
 * @see Sentence
 * @see Language
 */
public class WeightSet {

	/**
	 * When the {@link Word}s have the same normal form (infinitive).
	 */
	public float WordSame = 3;

	/**
	 * When the {@link Word}s were written in exactly the same form.
	 */
	public float WordExact = 2;

	/**
	 * When one {@link Word} is the synonyme of the other.
	 */
	public float WordSynonyme = 3;

	/**
	 * Multiplier specifying the difference between the MainWord and the
	 * Compound.
	 */
	public float MainWordMultiplier = 2;

	/**
	 * Multiplier specifying the weight of the 'Action' in the
	 * {@link SentenceObjectSet}.
	 */
	public float ActionMultiplier = 5;

	/**
	 * Multiplier specifying the weight of the 'Who' in the
	 * {@link SentenceObjectSet}.
	 */
	public float WhoMultiplier = 3;

	/**
	 * Multiplier specifying the weight of the 'What' in the
	 * {@link SentenceObjectSet}.
	 */
	public float WhatMultiplier = 3;

	/**
	 * Multiplier specifying the weight of the 'How' in the
	 * {@link SentenceObjectSet}.
	 */
	public float HowMultiplier = 5;

	/**
	 * Multiplier specifying the weight of the 'Else' in the
	 * {@link SentenceObjectSet}.
	 */
	public float ElseMultiplier = 1;

	/**
	 * Value specifying the weight of the length of the Action triggers.
	 */
	public float Length = 3;

	/**
	 * Multiplier specifying the weight of the count of parameters specified in
	 * the {@link Action}.
	 */
	public float ParameterCount = 10;

	/**
	 * Value specifying the weight when the last ContextStack item is in the
	 * same ActionPackage as the compared element.
	 */
	public float ContextPreviousSameActionPackage = 3;

	/**
	 * Value specifying the weight when the last ContextStack item has the same
	 * context as the compared element.
	 */
	public float ContextPreviousSameContext = 7;

	/**
	 * Multiplier when the context time difference is below 10s.
	 */
	public float ContextStageImmediateMultiplier = 3;

	/**
	 * Multiplier when the context time difference is below 60s.
	 */
	public float ContextStageCurrentMultiplier = 2;

	/**
	 * Multiplier when the context time difference is below 300s.
	 */
	public float ContextStageRecentMultiplier = 1;

	/**
	 * Multiplier when the context time difference is below 3600s.
	 */
	public float ContextStageOutdatedMultiplier = 0.5f;

	/**
	 * Constructor of a WeightSet with default values.
	 */
	public WeightSet() {
	}

	/**
	 * Constructor of a custom WeightSet.
	 * 
	 * @param wordSame
	 *            Value for {@link #WordSame} property.
	 * @param wordExact
	 *            Value for {@link #WordExact} property.
	 * @param wordSynonyme
	 *            Value for {@link #WordSynonyme} property.
	 * @param mainWordMultiplier
	 *            Value for {@link #MainWordMultiplier} property.
	 * @param actionMultiplier
	 *            Value for {@link #ActionMultiplier} property.
	 * @param whoMultiplier
	 *            Value for {@link #WhoMultiplier} property.
	 * @param whatMultiplier
	 *            Value for {@link #WhatMultiplier} property.
	 * @param howMultiplier
	 *            Value for {@link #HowMultiplier} property.
	 * @param elseMultiplier
	 *            Value for {@link #ElseMultiplier} property.
	 * @param length
	 *            Value for {@link #Length} property.
	 * @param parameterCount
	 *            Value for {@link #ParameterCount} property.
	 * @param contextPreviousSameActionPackage
	 *            Value for {@link #ContextPreviousSameActionPackage} property.
	 * @param contextPreviousSameContext
	 *            Value for {@link #ContextPreviousSameContext} property.
	 * @param contextStageImmediateMultiplier
	 *            Value for {@link #ContextStageImmediateMultiplier} property.
	 * @param contextStageCurrentMultiplier
	 *            Value for {@link #ContextStageCurrentMultiplier} property.
	 * @param contextStageRecentMultiplier
	 *            Value for {@link #ContextStageRecentMultiplier} property.
	 * @param contextStageOutdatedMultiplier
	 *            Value for {@link #ContextStageOutdatedMultiplier} property.
	 */
	public WeightSet(float wordSame, float wordExact, float wordSynonyme, float mainWordMultiplier,
			float actionMultiplier, float whoMultiplier, float whatMultiplier, float howMultiplier,
			float elseMultiplier, float length, float parameterCount, float contextPreviousSameActionPackage,
			float contextPreviousSameContext, float contextStageImmediateMultiplier,
			float contextStageCurrentMultiplier, float contextStageRecentMultiplier,
			float contextStageOutdatedMultiplier) {
		WordSame = wordSame;
		WordExact = wordExact;
		WordSynonyme = wordSynonyme;
		MainWordMultiplier = mainWordMultiplier;
		ActionMultiplier = actionMultiplier;
		WhoMultiplier = whoMultiplier;
		WhatMultiplier = whatMultiplier;
		HowMultiplier = howMultiplier;
		ElseMultiplier = elseMultiplier;
		Length = length;
		ParameterCount = parameterCount;
		ContextPreviousSameActionPackage = contextPreviousSameActionPackage;
		ContextPreviousSameContext = contextPreviousSameContext;
		ContextStageImmediateMultiplier = contextStageImmediateMultiplier;
		ContextStageCurrentMultiplier = contextStageCurrentMultiplier;
		ContextStageRecentMultiplier = contextStageRecentMultiplier;
		ContextStageOutdatedMultiplier = contextStageOutdatedMultiplier;
	}
}
