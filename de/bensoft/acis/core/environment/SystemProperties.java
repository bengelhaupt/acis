/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core.environment;

import java.util.Date;

/**
 * Contains properties about the system.
 *
 */
public class SystemProperties {

	private String mVersionString;
	private int mVersionCode;
	private String mBuildDateString;
	private Date mBuildDate;
	private String mDeveloper;
	private String mDescription;
	private long mSystemStartupTime;

	/**
	 * The constructor.
	 * 
	 * @param versionString
	 *            A String representing the version.
	 * @param versionCode
	 *            A number representing the version.
	 * @param buildDateString
	 *            A String representing the build date.
	 * @param buildDate
	 *            A timestamp representing the build time.
	 * @param developer
	 *            The developer.
	 * @param description
	 *            A description.
	 * @param systemStartupTime
	 *            The system startup time.
	 */
	public SystemProperties(String versionString, int versionCode, String buildDateString, Date buildDate,
			String developer, String description, long systemStartupTime) {
		mVersionString = versionString;
		mVersionCode = versionCode;
		mBuildDateString = buildDateString;
		mBuildDate = buildDate;
		mDeveloper = developer;
		mDescription = description;
		mSystemStartupTime = systemStartupTime;
	}

	/**
	 * Returns the version.
	 * 
	 * @return A String representing the version.
	 */
	public String getVersionString() {
		return mVersionString;
	}

	/**
	 * Returns the version.
	 * 
	 * @return A number representing the version.
	 */
	public int getVersionCode() {
		return mVersionCode;
	}

	/**
	 * Returns the build date.
	 * 
	 * @return A String representing the build date.
	 */
	public String getBuildDateString() {
		return mBuildDateString;
	}

	/**
	 * Returns the build date.
	 * 
	 * @return The build {@link Date}.
	 */
	public Date getBuildDate() {
		return mBuildDate;
	}

	/**
	 * Returns the developer.
	 * 
	 * @return The developer.
	 */
	public String getDeveloper() {
		return mDeveloper;
	}

	/**
	 * Returns a description.
	 * 
	 * @return The description.
	 */
	public String getDescription() {
		return mDescription;
	}

	/**
	 * Returns the system startup time.
	 * 
	 * @return A timestamp of the system start time.
	 */
	public long getSystemStartupTime() {
		return mSystemStartupTime;
	}
}