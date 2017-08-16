/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

import java.util.Locale;

import de.bensoft.acis.core.environment.Environment;
import de.bensoft.acis.core.language.Language;

/**
 * Used to represent an ActionPackage.<br>
 * An ActionPackage can contain several {@link Action}s which belong to the same
 * type of actions or are related to each other (like an application). The
 * {@link Action}s share the same {@link Environment}.<br>
 * An ActionPackage may not been added if the minimum {@link ACIS} library
 * version specified in {@link #getMinimumRequiredLibraryVersion()} is higher
 * than the version of the executing ACIS system (see
 * {@link ACIS#LIBRARY_VERSION}).
 */
public interface ActionPackage {

	/**
	 * Returns the name of the ActionPackage in Java-style (like
	 * com.example.acis.actions.whatever).
	 * 
	 * @return The name.
	 */
	String getName();

	/**
	 * Returns a short description of what the {@link Action}s in the package
	 * do.
	 * 
	 * @return The description.
	 */
	String getDescription();

	/**
	 * Returns a {@link Locale} for which the {@link Action}s are applicable.
	 * Usually this Locale represents the {@link Language} the {@link Action}s
	 * give output in.<br>
	 * {@link Action}s cannot be language-independent.
	 * 
	 * @return The language {@link Locale}.
	 */
	Locale getLocale();

	/**
	 * Returns the minimal required library version for this ActionPackage to
	 * work.<br>
	 * Version is in format &quot;yyMMdd&quot;. For all versions, return 0.
	 * 
	 * @return The version code of the minimum required ACIS system (e.g.
	 *         170810).
	 */
	int getMinimumRequiredLibraryVersion();

	/**
	 * Returns an array of {@link Action}s.
	 * 
	 * @param language
	 *            The {@link Language} the {@link ACIS} system to implement the
	 *            {@link Action}s in uses.
	 * @return The array of {@link Action}s.
	 * @throws ActionMalformedException
	 *             When one or more {@link Action}s are malformed.
	 */
	Action[] getActions(Language language) throws ActionMalformedException;
}