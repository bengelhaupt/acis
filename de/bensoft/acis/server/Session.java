/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.server;

/**
 * Represents a session on a server for a given user.
 *
 */
public class Session {

	private String mId;
	private long mTimeCreated;
	private String mUsername;

	/**
	 * The constructor for a new session for a user.
	 * 
	 * @param sessionId
	 *            The session ID.
	 * @param sessionTime
	 *            The time in milliseconds the session was created.
	 * @param username
	 *            The name of the {@link User} who created this session.
	 */
	public Session(String sessionId, long sessionTime, String username) {
		this.mId = sessionId;
		this.mTimeCreated = sessionTime;
		this.mUsername = username.toLowerCase();
	}

	/**
	 * Returns the session ID.
	 * 
	 * @return The session ID.
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Returns the time the session was created.
	 * 
	 * @return The time in milliseconds the session was created.
	 */
	public long getTimeCreated() {
		return mTimeCreated;
	}

	/**
	 * Returns the name of the {@link User}.
	 * 
	 * @return The name of the user who created this session.
	 */
	public String getUsername() {
		return mUsername;
	}
}
