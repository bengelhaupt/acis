/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.bensoft.acis.core.Action.ActionMethod;
import de.bensoft.acis.core.ActionResult.ActionResultCode;
import de.bensoft.acis.core.environment.Environment;
import de.bensoft.acis.core.environment.PackageEnvironment;
import de.bensoft.acis.core.environment.SystemEnvironment;
import de.bensoft.acis.core.language.Language;
import de.bensoft.acis.core.language.Sentence;
import de.bensoft.acis.core.language.SentenceObjectSet;
import de.bensoft.acis.core.language.WordCache;
import de.bensoft.acis.utils.Preferences;
import de.bensoft.acis.utils.IOUtils.SavingConfig;
import de.bensoft.acis.utils.Logging.Logger;
import de.bensoft.acis.utils.Logging.LoggingConfig;

/**
 * This class represents the base system. It is only applicable to one
 * {@link Language}.<br>
 * It manages the {@link Action}s, the {@link Environment}s for them and the
 * context system.<br>
 * The default {@link ContextStack} size is 30;<br>
 * The default data directory is "./".
 */
public class ACIS {

	/**
	 * The library's version.
	 */
	public static final int LIBRARY_VERSION = 170810; // yyMMdd, changes when
	// there are major API
	// changes

	private Language mLanguage;
	private SystemEnvironment mSystemEnvironment;
	private ContextStack mGeneralContext = new ContextStack(30);

	private Map<String, Context> mContexts = new HashMap<>(0);

	private File mDataDirectory;
	private File mActionsDataDirectory;

	private Logger mLogger;

	private WordCache mCache;

	private Preferences mSystemPreferences;

	private ActionManager mActionManager;

	private float mMatcherParameterThreshold = 0.3f;

	private final Thread mStartingThread;

	/**
	 * Constructor for a ACIS system.
	 * 
	 * @param language
	 *            The {@link Language} to use.
	 * @param environment
	 *            The {@link SystemEnvironment} to use.
	 * @throws IOException
	 *             When there is no way to save files.
	 */
	public ACIS(Language language, SystemEnvironment environment) throws IOException {
		mStartingThread = Thread.currentThread();
		mLanguage = language;
		mSystemEnvironment = environment;
		setDataDirectory(new File("./"));
	}

	/**
	 * Constructor for a ACIS system.
	 * 
	 * @param language
	 *            The {@link Language} to use.
	 * @param environment
	 *            The {@link SystemEnvironment} to use.
	 * @param dataDirectory
	 *            The directory to save the systems data in.
	 * @throws IOException
	 *             When the {@code dataDirectory} is no directory.
	 */
	public ACIS(Language language, SystemEnvironment environment, File dataDirectory) throws IOException {
		mStartingThread = Thread.currentThread();
		mLanguage = language;
		mSystemEnvironment = environment;
		setDataDirectory(dataDirectory);
	}

	/**
	 * Returns the data directory the system uses.
	 * 
	 * @return The data directory.
	 */
	public File getDataDirectory() {
		return mDataDirectory;
	}

	/**
	 * Returns the directory in which the packages containing actions are saved.
	 * 
	 * @return The package files directory.
	 */
	public File getPackageFilesDirectory() {
		return new File(mDataDirectory.getAbsolutePath() + "/packages");
	}

	/**
	 * Sets the data directory in which the system writes its data.
	 * 
	 * @param dataDirectory
	 *            The directory.
	 * @throws IOException
	 *             When the {@code dataDirectory} is no directory.
	 */
	public void setDataDirectory(File dataDirectory) throws IOException {
		if (!dataDirectory.isDirectory())
			throw new IOException("Specified java.io.File is not a directory.");

		mDataDirectory = dataDirectory;
		mActionsDataDirectory = new File(dataDirectory.getAbsolutePath() + "/data");
		mLogger = new Logger(new LoggingConfig(new File(mDataDirectory.getAbsolutePath() + "/system.log")));
		mSystemPreferences = new Preferences(
				new SavingConfig(new File(mDataDirectory.getAbsolutePath() + "/system.pref")));
		mCache = new WordCache(
				new SavingConfig(new File(mDataDirectory.getAbsolutePath() + "/" + mLanguage.getName() + ".cache")));
		mCache.setLogger(new Logger(new LoggingConfig(
				new File(mDataDirectory.getAbsolutePath() + "/" + mLanguage.getName() + ".cache.log"))));
		mLanguage.setWordCache(mCache);
		mActionManager = new ActionManager(mLogger);
		if (!getPackageFilesDirectory().exists())
			getPackageFilesDirectory().mkdirs();
	}

	/**
	 * Returns the {@link Language} used by the system.
	 * 
	 * @return The Language interface Object.
	 */
	public Language getLanguage() {
		return mLanguage;
	}

	/**
	 * Returns the {@link SystemEnvironment} used by the system.
	 * 
	 * @return The system environment.
	 */
	public SystemEnvironment getSystemEnvironment() {
		return mSystemEnvironment;
	}

	/**
	 * Returns the {@link Environment} for a specific {@link ActionPackage}
	 * using the {@link SystemEnvironment} which was previously set in the
	 * constructor.
	 * 
	 * @param pack
	 *            The {@link ActionPackage}.
	 * @return The {@link Environment} specifically for this
	 *         {@link ActionPackage}.
	 */
	public Environment getEnvironment(final ActionPackage pack) {
		return getEnvironment(pack, mSystemEnvironment);
	}

	/**
	 * Returns the {@link Environment} for a specific {@link ActionPackage}
	 * using an other {@link SystemEnvironment} than the one of this ACIS
	 * system..
	 * 
	 * @param pack
	 *            The {@link ActionPackage}.
	 * @param systemEnvironment
	 *            The {@link SystemEnvironment}.
	 * @return The {@link Environment} specifically for this
	 *         {@link ActionPackage}.
	 */
	public Environment getEnvironment(final ActionPackage pack, final SystemEnvironment systemEnvironment) {
		for (Action a : getActionManager().getActions()) {
			if (a.getPackage().getName().equals(pack.getName())) {
				return EnvironmentCreator.createEnvironment(systemEnvironment, new PackageEnvironment() {

					@Override
					public File getDataDirectory() {
						File dataDir = new File(mActionsDataDirectory.getAbsoluteFile() + "/" + pack.getName());
						if (!dataDir.exists())
							dataDir.mkdirs();
						return dataDir;
					}

					@Override
					public Preferences getPreferences() {
						try {
							Preferences pref = new Preferences(new SavingConfig(
									new File(getDataDirectory().getAbsolutePath() + "/preferences.pref")));
							return pref;
						} catch (IOException e) {
							mLogger.e("ACIS",
									"Preferences for '" + pack.getName() + "' could not be created: " + e.toString());
						}
						return null;
					}

					@Override
					public Language getLanguage() {
						return mLanguage;
					}

					@Override
					public ContextStack getContextStack() {
						ContextStack copy = new ContextStack(getGeneralContext().getMaximumSize(),
								getGeneralContext().getItems());
						for (int i = 0; i < copy.getItems().length; i++) {
							ContextStackItem item = copy.getItems()[i];
							if (item.getAction().getContextVisibility() == ContextVisibility.PRIVATE
									|| (item.getAction().getContextVisibility() == ContextVisibility.PACKAGE
											&& !item.getAction().getPackage().equals(pack))) {
								copy.removeItem(i);
								i--;
							}

							if (item.getAction().getContextVisibility() == ContextVisibility.PUBLIC_NO_PARAMETERS)
								copy.updateItem(i,
										new ContextStackItem(item.getTime(), item.getAction(),
												new Sentence(item.getAction().getTrigger()).getTextWithoutParams(),
												item.getActionResult()));
						}
						return copy;
					}

				});
			}
		}
		return null;
	}

	/**
	 * Returns the system {@link de.bensoft.acis.utils.Logging.Logger}.
	 * 
	 * @return The {@link de.bensoft.acis.utils.Logging.Logger} Object.
	 */
	public Logger getLogger() {
		return mLogger;
	}

	/**
	 * Returns the systems {@link de.bensoft.acis.utils.Preferences}.
	 * 
	 * @return The {@link de.bensoft.acis.utils.Preferences} Object.
	 */
	public Preferences getSystemPreferences() {
		return mSystemPreferences;
	}

	/**
	 * Returns the {@link ActionManager} of the system in which all
	 * {@link Action}s are registered.
	 * 
	 * @return The {@link ActionManager}.
	 */
	public ActionManager getActionManager() {
		return mActionManager;
	}

	/**
	 * Returns the {@link WordCache} for the specific {@link Language} used in
	 * the system.
	 * 
	 * @return The {@link WordCache} Object.
	 */
	public WordCache getWordCache() {
		return mCache;
	}

	/**
	 * Returns a {@link Matcher} Object to use for {@link Action} comparison.
	 * 
	 * @return The {@link Matcher} Object.
	 */
	private Matcher getMatcher() {
		Matcher matcher = new Matcher(mLanguage, mActionManager.getActions(mContexts), mMatcherParameterThreshold);
		matcher.setLogger(mLogger);
		return matcher;
	}

	/**
	 * A function which returns the parameter matching threshold. This value
	 * describes the score threshold under which no parameter analysis is
	 * performed. This setting is due to performance improvements, because
	 * otherwise a parameter parsing would be performed on all Actions. Note:
	 * The score after length and word comparison is used as reference.
	 * 
	 * @return The parameter matching threshold. Default is 0.3.
	 */
	public float getMatcherParameterThreshold() {
		return mMatcherParameterThreshold;
	}

	/**
	 * Sets the parameter matching threshold. This value describes the score
	 * threshold under which no parameter analysis is performed. This setting is
	 * due to performance improvements, because otherwise a parameter parsing
	 * would be performed on all Actions. Note: The score after length and word
	 * comparison is used as reference.
	 * 
	 * @param matcherParameterThreshold
	 *            The parameter threshold. Value must be between 0 and 1.
	 */
	public void setMatcherParameterThreshold(float matcherParameterThreshold) {
		mMatcherParameterThreshold = matcherParameterThreshold;
	}

	/**
	 * Returns the {@link ContextStack} including all context items (even
	 * {@link ContextVisibility#PRIVATE}).
	 * 
	 * @return The {@link ContextStack}.
	 */
	public ContextStack getGeneralContext() {
		return mGeneralContext;
	}

	/**
	 * Sets the size of the {@link ContextStack}.<br>
	 * Note: Previous entries are lost.
	 * 
	 * @param maxSize
	 *            The size to set.
	 */
	public void setGeneralContextMaximumSize(int maxSize) {
		mGeneralContext = new ContextStack(maxSize);
	}

	/**
	 * Matches the input and executes the best {@link Action} (if not
	 * overwritten in {@link OnExecutionListener}).<br>
	 * Note: This function must be executed on a Thread other than the Thread
	 * the underlying {@link ACIS} Object was created with.
	 * 
	 * @param input
	 *            The user input.
	 * @param threshold
	 *            The threshold for which {@link Action}s with a lower match
	 *            score are ignored.
	 * @param weightSet
	 *            The {@link WeightSet} to use for matching.
	 * @param executionListener
	 *            The listener for the execution events. May be {@code null}.
	 * @return The ActionResult of the executed Action or {@code null} when
	 *         there were no results or no ActionResult given by the methods of
	 *         the {@code executionListener}.
	 * @throws IllegalThreadStateException
	 *             When executed on the same Thread the ACIS Object was created
	 *             in.
	 */
	public ActionResult execute(String input, float threshold, WeightSet weightSet,
			OnExecutionListener executionListener) throws IllegalThreadStateException {
		if (mStartingThread == Thread.currentThread())
			throw new IllegalStateException("This function must be executed on a spearate Thread.");

		if (executionListener == null)
			executionListener = new OnExecutionListener() {
			};

		Sentence sentence = getLanguage().getSentence(input);
		SentenceObjectSet sentenceObjects = getLanguage().getSentenceObjects(sentence);

		MatchResult[] results = getMatcher().getMatches(sentence, sentenceObjects, getGeneralContext(), threshold,
				weightSet);

		results = executionListener.onMatcherResult(results);

		MatchResult best = executionListener.onGetBestResult(results);
		if (best != null) {
			Action action = best.getAction();
			ActionResult actionResult = executionListener.onActionRun(action, getEnvironment(action.getPackage()),
					sentence, best.getParameter());
			if (actionResult != null) {
				if (action instanceof ContextConstructorAction
						&& actionResult.getResultCode() == ActionResultCode.CREATE_CONTEXT) {
					Context c = new Context(((ContextConstructorAction) action).getContextId(),
							((ContextConstructorAction) action).getContextExpirationTime(), System.currentTimeMillis());
					mContexts.put(c.getId(), c);
				}

				if (action instanceof ContextDependentAction && !(action instanceof ContextDestructorAction)) {
					Context c = mContexts.get(((ContextDependentAction) action).getDependingContextId());
					c.renew();
					mContexts.put(c.getId(), c);
				}

				if (action instanceof ContextDestructorAction
						&& actionResult.getResultCode() == ActionResultCode.DESTROY_CONTEXT) {
					mContexts.remove(((ContextDestructorAction) action).getDependingContextId());
				}

				ContextStackItem contextItem = new ContextStackItem(System.currentTimeMillis(), best.getAction(), input,
						actionResult);
				getGeneralContext().addItem(contextItem);

				return actionResult;
			}
		}
		return null;
	}

	/**
	 * Matches the input and executes the best {@link Action} (if not
	 * overwritten in {@link OnExecutionListener}).<br>
	 * For this a new {@link Thread} is started.
	 * 
	 * @param input
	 *            The user input.
	 * @param threshold
	 *            The threshold for which {@link Action}s with a lower match
	 *            score are ignored.
	 * @param weightSet
	 *            The {@link WeightSet} to use for matching.
	 * @param executionListener
	 *            The listener for the execution events. May be {@code null}.
	 * @return The Thread of execution.
	 */
	public Thread executeNewThread(final String input, final float threshold, final WeightSet weightSet,
			OnExecutionListener executionListener) {
		final OnExecutionListener listener;
		if (executionListener == null)
			listener = new OnExecutionListener() {
			};
		else
			listener = executionListener;

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				execute(input, threshold, weightSet, listener);
			}
		});
		thread.start();
		return thread;
	}

	/**
	 * Represents a listener for events in the
	 * {@link ACIS#executeNewThread(String, float, WeightSet, OnExecutionListener)}
	 * function.
	 *
	 */
	public static abstract class OnExecutionListener {

		/**
		 * Called when the matching is completed.<br>
		 * By default this sorts the results descending by score.<br>
		 * 
		 * @param results
		 *            The matching results. May be of length 0;
		 * @return The {@link MatchResult} array (Must not be {@code null},
		 *         empty array instead).
		 */
		public MatchResult[] onMatcherResult(MatchResult[] results) {
			List<MatchResult> list = Arrays.asList(results);
			list.sort(new Comparator<MatchResult>() {

				@Override
				public int compare(MatchResult o1, MatchResult o2) {
					if (o1.getScore() > o2.getScore())
						return -1;
					if (o1.getScore() < o2.getScore())
						return 1;
					return 0;
				}

			});
			return list.toArray(new MatchResult[0]);
		}

		/**
		 * Called when the best result is about o be selected.<br>
		 * Note: Do not forget to call {@code super} if not doing the
		 * MatchResult selection yourself.<br>
		 * By default this returns the element with the highest score.
		 * 
		 * @param results
		 *            The matching results sorted descending by score. May be of
		 *            length 0;
		 * @return The {@link MatchResult} of which the {@link Action} should be
		 *         executed or {@code null} when no best result was found (e.g.
		 *         in case of a 0-length {@code results} array).
		 */
		public MatchResult onGetBestResult(MatchResult[] results) {
			if (results.length == 0)
				return null;
			return results[0];
		}

		/**
		 * Runs the Action's {@link ActionMethod} with the given parameters.<br>
		 * Note: Do not forget to call {@code super} if not running the
		 * ActionMethod yourself.<br>
		 * By default this simply calls the
		 * {@link ActionMethod#run(Environment, Sentence, Parameter[])} method.
		 * 
		 * @param action
		 *            The {@link Action}.
		 * @param environment
		 *            The {@link Environment}.
		 * @param sentence
		 *            The input {@link Sentence}.
		 * @param parameter
		 *            The parsed {@link Parameter}s.
		 * @return The {@link ActionResult} of the execution.
		 */
		public ActionResult onActionRun(Action action, Environment environment, Sentence sentence,
				Parameter[] parameter) {
			return action.getActionMethod().run(environment, sentence, parameter);
		}
	}
}