/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import de.bensoft.acis.utils.IOUtils.SavingConfig;

/**
 * Represents a class to help storing settings (preferences) in a file.<br>
 * A preference consists of a unique name and a corresponding value.<br>
 * The preferences file is UTF-8 encoded.
 *
 */
public class Preferences {

	private SavingConfig preferenceConfig;

	/**
	 * Creates a new Preferences Object using the file in the
	 * {@link IOUtils.SavingConfig}.
	 * 
	 * @param config
	 *            The {@link IOUtils.SavingConfig} to use.
	 */
	public Preferences(SavingConfig config) {
		preferenceConfig = config;
	}

	/**
	 * Sets the {@link IOUtils.SavingConfig}.
	 * 
	 * @param config
	 *            The {@link IOUtils.SavingConfig} to set.
	 */
	public void setPreferenceConfig(SavingConfig config) {
		preferenceConfig = config;
	}

	/**
	 * Returns the used {@link IOUtils.SavingConfig}.
	 * 
	 * @return The {@link IOUtils.SavingConfig}.
	 */
	public SavingConfig getPreferenceConfig() {
		return preferenceConfig;
	}

	/**
	 * Fetches the value of the given preference's name.
	 * 
	 * @param name
	 *            The name of the preference to return the value of.
	 * @param def
	 *            The value that is returned when the preference with
	 *            {@code name} could not be found.
	 * @return The value for the preference {@code name} or {@code def} when
	 *         {@code name} could not be found.
	 */
	public String get(String name, String def) {
		String f = IOUtils.readSingleLineFromFile(getPreferenceConfig().getFile());
		String[] el = f.split("<pref>");
		for (String s : el) {
			String[] split = s.split("<br>");
			if (split[0].equals(name))
				try {
					return URLDecoder.decode(split[1], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
		}
		return def;
	}

	/**
	 * Creates a new preference entry with a {@code name} and a {@code value} or
	 * overrides the {@code value} if a preference entry with the same
	 * {@code name} already exists.
	 * 
	 * @param name
	 *            The name of the preference to write.
	 * @param value
	 *            The corresponding value. Old value will be overridden if
	 *            {@code name} already exists.
	 */
	public void write(String name, String value) {
		try {
			remove(name);
			String element = URLEncoder.encode(name, "UTF-8") + "<br>" + URLEncoder.encode(value, "UTF-8") + "<pref>";
			FileWriter fw = new FileWriter(getPreferenceConfig().getFile(), true);
			fw.append(element);
			fw.flush();
			fw.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks whether a preference entry exists.
	 * 
	 * @param name
	 *            The name of the preference to check for.
	 * @return {@code true} when it exists, else {@code false}.
	 */
	public boolean has(String name) {
		String f = IOUtils.readSingleLineFromFile(getPreferenceConfig().getFile());
		String[] el = f.split("<pref>");
		for (String s : el) {
			String[] split = s.split("<br>");
			if (split[0].equals(name))
				return true;
		}
		return false;
	}

	/**
	 * Removes a preference entry.
	 * 
	 * @param name
	 *            The name of the preference to remove.
	 */
	public void remove(String name) {
		try {
			String all = IOUtils.readSingleLineFromFile(getPreferenceConfig().getFile());
			String[] el = all.split("<pref>");
			for (int i = 0; i < el.length; i++) {
				String[] split = el[i].split("<br>");
				if (split[0].equals(name)) {
					all = "";
					for (int j = 0; j < el.length; j++) {
						if (j != i)
							all += el[j] + "<pref>";
					}
					break;
				}
			}
			FileWriter fw = new FileWriter(getPreferenceConfig().getFile(), false);
			fw.write(all);
			fw.flush();
			fw.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}