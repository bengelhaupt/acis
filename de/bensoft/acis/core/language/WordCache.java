/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core.language;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import de.bensoft.acis.utils.Logging.Loggable;
import de.bensoft.acis.utils.IOUtils.SavingConfig;

/**
 * Represents a class for caching the information from {@link Word} Objects.<br>
 * It uses a text file for caching.
 *
 */
public class WordCache extends Loggable {

	private SavingConfig mConfig;
	private boolean mEnabled = true;

	/**
	 * Creates a new instance using the given
	 * {@link de.bensoft.acis.utils.IOUtils.SavingConfig}.
	 * 
	 * @param config
	 *            The saving configuration.
	 */
	public WordCache(SavingConfig config) {
		mConfig = config;
	}

	/**
	 * Returns the used {@link de.bensoft.acis.utils.IOUtils.SavingConfig}.
	 * 
	 * @return The saving configuration.
	 */
	public SavingConfig getSavingConfig() {
		return mConfig;
	}

	/**
	 * Returns whether the caching is enabled.
	 * 
	 * @return {@code true} when enabled, else {@code false}
	 */
	public boolean isEnabled() {
		return mEnabled;
	}

	/**
	 * Enables or disables caching.
	 * 
	 * @param enabled
	 *            Whether caching should be enabled.
	 */
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	/**
	 * Writes the information of a {@link Word} Object in the cache (when its
	 * enabled).<br>
	 * Writes nothing if {@code word} is {@code null}.<br>
	 * Warning: This function writes regardless of the fact whether the Word is
	 * already in the cache.
	 * 
	 * @param word
	 *            The {@link Word} to write.
	 */
	public void writeInCache(Word word) {
		if (word == null) {
			getLogger().e("WORD_CACHING",
					String.format(CacheLoggingMessages.CACHE_WRITE_ERROR, "null", "The word is null"));
			return;
		}
		if (mEnabled)
			try {
				Writer fw = new OutputStreamWriter(new FileOutputStream(mConfig.getFile(), true),
						StandardCharsets.UTF_8);
				String syns = "";
				for (String s : word.getSynonyms()) {
					syns += s + ";";
				}
				fw.append(
						word.getTypedForm() + "#" + word.getNormalForm() + "#" + word.getType() + "#" + syns + "<END>");
				fw.close();
				getLogger().i("WORD_CACHING",
						String.format(CacheLoggingMessages.CACHE_WRITE_SUCCESS, word.getTypedForm()));
			} catch (Exception ex) {
				getLogger().e("WORD_CACHING",
						String.format(CacheLoggingMessages.CACHE_WRITE_ERROR, word.getTypedForm(), ex.getMessage()));
			}
	}

	/**
	 * Reads a {@link Word} Object from the cache.
	 * 
	 * @param word
	 *            The typed form of the word to read.
	 * @return The {@link Word} Object including all its information.
	 * @throws IllegalArgumentException
	 *             When {@code word} is not present in the cache.
	 */
	public Word readFromCache(String word) throws IllegalArgumentException {
		Word w = null;
		try {
			InputStream inputStream = new FileInputStream(mConfig.getFile());
			BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			StringBuilder total = new StringBuilder();
			String l;
			while ((l = r.readLine()) != null) {
				total.append(l);
			}
			r.close();

			String f = total.toString();

			String[] lines = f.split("<END>");
			for (String line : lines) {

				String[] splitted = line.split("#");
				if (splitted[0].equals(word)) {
					String[] syns = {};
					if (splitted.length > 3)
						syns = splitted[3].split(";");
					w = new Word(splitted[0], splitted[1], Integer.parseInt(splitted[2]), syns);
				}
			}
			if (w == null)
				throw new IllegalArgumentException("The word " + word + " was not found in the cache");
			getLogger().i("WORD_CACHING", String.format(CacheLoggingMessages.CACHE_READ_SUCCESS, word));
		} catch (Exception ex) {
			getLogger().e("WORD_CACHING", String.format(CacheLoggingMessages.CACHE_READ_ERROR, word, ex.getMessage()));
		}

		return w;
	}

	/**
	 * Checks whether a word is present in the cache.
	 * 
	 * @param word
	 *            The typed form of the word to check for.
	 * @return {@code true} when it is present, else {@code false}.
	 */
	public boolean isInCache(String word) {
		try {
			InputStream inputStream = new FileInputStream(mConfig.getFile());
			BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			StringBuilder total = new StringBuilder();
			String l;
			while ((l = r.readLine()) != null) {
				total.append(l);
			}
			r.close();

			String f = total.toString();

			String[] lines = f.split("<END>");
			for (String line : lines) {
				if (line.split("#")[0].equals(word)) {
					getLogger().i("WORD_CACHING",
							String.format(CacheLoggingMessages.CACHE_CONTAINS_SUCCESS, word, true));
					return true;
				}
			}
		} catch (Exception ex) {
			getLogger().e("WORD_CACHING",
					String.format(CacheLoggingMessages.CACHE_CONTAINS_ERROR, word, ex.getMessage()));
		}

		getLogger().i("WORD_CACHING", String.format(CacheLoggingMessages.CACHE_CONTAINS_SUCCESS, word, false));
		return false;
	}

	/**
	 * Contains several predefined messages for logging.
	 *
	 */
	private class CacheLoggingMessages {

		public final static String CACHE_WRITE_SUCCESS = "The Word '%1$s' was successfully written into the cache.";
		public final static String CACHE_WRITE_ERROR = "An error occured while writing the Word '%1$s' into the cache: %2$s";
		public final static String CACHE_READ_SUCCESS = "The Word '%1$s' was successfully read from the cache.";
		public final static String CACHE_READ_ERROR = "An error occured while reading the Word '%1$s' from the cache: %2$s";
		public final static String CACHE_CONTAINS_SUCCESS = "Checking if cache contains the Word '%1$s' was successful: %2$b";
		public final static String CACHE_CONTAINS_ERROR = "An error occured while checking if cache contains the Word '%1$s' : %2$s";

	}
}