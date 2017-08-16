/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core.environment;

/**
 * Contains information about the user.
 *
 */
public class UserInfo {

	private String mSurname;
	private String mName;
	private String[] mNicknames;
	private int mAge;
	private String mEmail;
	private String mAddress;

	/**
	 * The constructor.
	 * 
	 * @param surname
	 *            The surname.
	 * @param name
	 *            The name.
	 * @param nicknames
	 *            A nickname array.
	 * @param age
	 *            The age.
	 * @param email
	 *            A E-Mail address.
	 * @param address
	 *            The address (home location).
	 */
	public UserInfo(String surname, String name, String[] nicknames, int age, String email, String address) {
		mSurname = surname;
		mName = name;
		mNicknames = nicknames;
		mAge = age;
		mEmail = email;
		mAddress = address;
	}

	/**
	 * Returns the surname.
	 * 
	 * @return The surname.
	 */
	public String getSurname() {
		return mSurname;
	}

	/**
	 * Returns the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Returns the nicknames.
	 * 
	 * @return A String array containing the nicknames.
	 */
	public String[] getNicknames() {
		return mNicknames;
	}

	/**
	 * Returns the age.
	 * 
	 * @return The age.
	 */
	public int getAge() {
		return mAge;
	}

	/**
	 * Returns the E-Mail.
	 * 
	 * @return The E-Mail.
	 */
	public String getEmail() {
		return mEmail;
	}

	/**
	 * Returns the address (home location).
	 * 
	 * @return The address.
	 */
	public String getAddress() {
		return mAddress;
	}
}
