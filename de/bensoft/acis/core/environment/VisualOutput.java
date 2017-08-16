/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core.environment;

/**
 * Represents a unified interface for visual output.<br>
 * It can be used to show a specific 'visual' or set a property of the output
 * (such as LCD brightness).
 * <p>
 * The supported visuals and properties can be accessed through the respective
 * function calls.
 * </p>
 * Standard visual names are TEXT, IMAGE. (About to be extended)
 *
 */
public interface VisualOutput {

	/**
	 * Outputs a specific visual with a set of arguments.
	 * 
	 * @param visualName
	 *            The name of the visual.
	 * @param args
	 *            The array of arguments to be passed.
	 * @throws IllegalArgumentException
	 *             When there is no such visual with {@code visualName}.
	 */
	public void show(String visualName, Object[] args) throws IllegalArgumentException;

	/**
	 * Sets a property of the output.
	 * 
	 * @param propertyName
	 *            The name of the property.
	 * @param value
	 *            The value to set.
	 * @throws IllegalArgumentException
	 *             When there is no such property with {@code propertyName}.
	 */
	public void setProperty(String propertyName, Object value) throws IllegalArgumentException;

	/**
	 * Returns the available properties for the output.
	 * 
	 * @return An array of property names.
	 */
	public String[] getAvailableProperties();

	/**
	 * Returns the available visuals for the output.
	 * 
	 * @return An array of visual names.
	 */
	public String[] getAvailableVisuals();

	/**
	 * Returns the valid values to be used when calling
	 * {@link VisualOutput#setProperty(String, Object)}.
	 * 
	 * @param propertyName
	 *            The name of the property.
	 * @return An array of values for {@code propertyName}.
	 */
	public String[] getValuesForProperty(String propertyName);

	/**
	 * Returns the arguments to be used when calling
	 * {@link #show(String, Object[])}.
	 * 
	 * @param visualName
	 *            The name of the visual to request the arguments for.
	 * @return An array of arguments for {@code visualName}.
	 */
	public String[] getArgumentsForVisual(String visualName);
}