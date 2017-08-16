/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

/**
 * Represents a parameter in a {@link Action}.<br>
 * A parameter defined in the {@link Action} trigger will be parsed in such an
 * Object.
 *
 */
class ActionParameter {

	private ParameterType mType;
	private int mIndex;
	private String mPreDelimiter;
	private String mPostDelimiter;

	/**
	 * The ActionParameter constructor.
	 * 
	 * @param type
	 *            The type. See {@link ParameterType} for more information.
	 * @param preDelimiter
	 *            The word String before the parameter declaration. May be
	 *            {@code null}.
	 * @param postDelimiter
	 *            The word String after the parameter declaration. May be
	 *            {@code null}.
	 * @param index
	 *            The specified index.
	 */
	public ActionParameter(ParameterType type, String preDelimiter, String postDelimiter, int index) {
		mType = type;
		mIndex = index;
		mPreDelimiter = preDelimiter;
		mPostDelimiter = postDelimiter;
	}

	/**
	 * A function which returns the type of the ActionParameter.
	 * 
	 * @return The parameter type.
	 */
	public ParameterType getType() {
		return mType;
	}

	/**
	 * A function which returns the index of the ActionParameter.
	 * 
	 * @return The specified index.
	 */
	public int getIndex() {
		return mIndex;
	}

	/**
	 * A function which returns the pre-delimiter of the ActionParameter.
	 * 
	 * @return The word String before the parameter declaration. May be
	 *         {@code null}.
	 */
	public String getPreDelimiter() {
		return mPreDelimiter;
	}

	/**
	 * A function which returns the post-delimiter of the ActionParameter.
	 * 
	 * @return The word String after the parameter declaration. May be
	 *         {@code null}.
	 */
	public String getPostDelimiter() {
		return mPostDelimiter;
	}
}