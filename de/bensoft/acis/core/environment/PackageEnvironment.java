/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core.environment;

import java.io.File;

import de.bensoft.acis.core.ActionPackage;
import de.bensoft.acis.core.ContextStack;
import de.bensoft.acis.core.language.Language;
import de.bensoft.acis.utils.Preferences;

/**
 * Represents the parts of an {@link Environment} which are individual for each
 * {@link ActionPackage}.
 *
 */
public interface PackageEnvironment {

	/**
	 * Returns the data directory in which any files should be written.
	 * 
	 * @return The data directory.
	 */
	public File getDataDirectory();

	/**
	 * Returns the {@link Preferences} for this specific {@link ActionPackage}.
	 * 
	 * @return The {@link Preferences}.
	 */
	public Preferences getPreferences();

	/**
	 * Returns the {@link Language} used by the system the {@link ActionPackage}
	 * is added to.
	 * 
	 * @return The {@link Language} interface Object.
	 */
	public Language getLanguage();

	/**
	 * Returns the current {@link ContextStack} which includes the
	 * {@link de.bensoft.acis.core.ContextStackItem}s.
	 * 
	 * @return The {@link ContextStack}.
	 */
	public ContextStack getContextStack();
}