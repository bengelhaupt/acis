/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

/**
 * Represents a context element.
 *
 */
public class Context {

	private String mId;
	private long mExpirationTime;
	private long mCreationTime;

	/**
	 * The constructor.
	 * 
	 * @param id
	 *            The id.
	 * @param expirationTime
	 *            The time in milliseconds after which the context expires and
	 *            is no longer valid.
	 * @param creationTime
	 *            The time the context was created or the last renew action.
	 */
	public Context(String id, long expirationTime, long creationTime) {
		mId = id;
		mExpirationTime = expirationTime;
		mCreationTime = creationTime;
	}

	/**
	 * Returns the id.
	 * 
	 * @return The id.
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Returns the time after which the context expires and is no longer valid.
	 * 
	 * @return The expiration time in milliseconds.
	 */
	public long getExpirationTime() {
		return mExpirationTime;
	}

	/**
	 * Returns the time of context creation or when it was last renewed.
	 * 
	 * @return The creation time in milliseconds.
	 */
	public long getCreationTime() {
		return mCreationTime;
	}
	
	/**
	 * Renews the context.
	 */
	public void renew(){
		mCreationTime = System.currentTimeMillis();
	}
}