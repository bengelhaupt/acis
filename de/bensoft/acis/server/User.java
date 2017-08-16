/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.server;

/**
 * Represents a user for the server. It has a name, password and restricted
 * paths as properties.
 *
 */
public class User {

	private String mName;
	private String mPassword;
	private String[] mRestrictedPaths = new String[] {};

	/**
	 * Creates a new User using the given {@code name} and {@code password}.
	 * 
	 * @param name
	 *            The name.
	 * @param password
	 *            The corresponding password (usually a MD5).
	 */
	public User(String name, String password) {
		mName = name;
		mPassword = password;
	}

	/**
	 * Creates a new User using the given {@code name}, {@code password} and the
	 * restricted paths.
	 * 
	 * @param name
	 *            The name.
	 * @param password
	 *            The corresponding password (usually a MD5).
	 * @param restrictedPaths
	 *            An array of relative paths this User should have no access at.
	 *            Must not be {@code null}.
	 */
	public User(String name, String password, String[] restrictedPaths) {
		mName = name;
		mPassword = password;
		mRestrictedPaths = restrictedPaths;
	}

	/**
	 * Returns the name of the User.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Returns te password of the User.
	 * 
	 * @return The password.
	 */
	public String getPassword() {
		return mPassword;
	}

	/**
	 * Returns the restricted paths of the User.
	 * 
	 * @return An array containing the restricted paths.
	 */
	public String[] getRestrictedPaths() {
		return mRestrictedPaths;
	}

	/**
	 * Sets the restricted paths for the User.
	 * 
	 * @param restrictedPaths
	 *            The restricted paths to set.
	 */
	public void setRestrictedPaths(String[] restrictedPaths) {
		mRestrictedPaths = restrictedPaths;
	}
}