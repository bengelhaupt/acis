/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

/**
 * Represents a successfully parsed parameter including the value.
 *
 */
public class Parameter {

	private ParameterType mParameterType;
	private int mIndex;
	private String mValue;

	/**
	 * The constructor of a Parameter.
	 * 
	 * @param type
	 *            The type.
	 * @param index
	 *            The index.
	 * @param value
	 *            The value which was represented by the parameter declaration
	 *            in the {@link Action} trigger.
	 */
	public Parameter(ParameterType type, int index, String value) {
		mParameterType = type;
		mIndex = index;
		mValue = value;
	}

	/**
	 * Returns the type of the Parameter.
	 * 
	 * @return The {@link ParameterType}.
	 */
	public ParameterType getParameterType() {
		return mParameterType;
	}

	/**
	 * Returns the index of the Parameter.
	 * 
	 * @return The index.
	 */
	public int getIndex() {
		return mIndex;
	}

	/**
	 * Returns the value which was previously represented by the parameter
	 * declaration in the {@link Action} trigger.
	 * 
	 * @return The value String.
	 */
	public String getValue() {
		return mValue;
	}
}