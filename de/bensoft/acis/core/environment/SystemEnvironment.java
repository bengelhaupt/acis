/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core.environment;

/**
 * Represents a globally available unified interface for Actions. It supports
 * spoken, written and visual output functions.
 *
 */
public interface SystemEnvironment {

	/**
	 * Returns a {@link SystemProperties} Object containing some system
	 * properties.
	 * 
	 * @return The {@link SystemProperties} Object.
	 */
	public SystemProperties getSystemProperties();

	/**
	 * Returns a {@link UserInfo} Object which contains information about the
	 * user.
	 * 
	 * @return The {@link UserInfo} Object.
	 */
	public UserInfo getUserInfo();

	/**
	 * Returns whether speech output is available.
	 * 
	 * @return {@code true} if available, else {@code false}.
	 */
	public boolean canSpeak();

	/**
	 * Adds written and spoken output.
	 * 
	 * Note: There should not be multiple calls of this function sequentially
	 * because of there may be a unexpected behavior. The output should be
	 * wrapped and this function called once at a time.
	 * 
	 * @param output
	 *            The output.
	 */
	public void addOutput(String output);

	/**
	 * Add written output.
	 * 
	 * @param output
	 *            The output.
	 */
	public void addWrittenOutput(String output);

	/**
	 * Adds speech output.
	 * 
	 * Note: There should not be multiple calls of this function sequentially
	 * because of there may be a unexpected behavior. The output should be
	 * wrapped and this function called once at a time.
	 * 
	 * @param output
	 *            The speech output.
	 */
	public void addSpokenOutput(String output);

	/**
	 * Returns whether input can be requested.
	 * 
	 * @return {@code true} if yes, else {@code false}.
	 */
	public boolean canRequestInput();

	/**
	 * Requests input from the user and returns the result.
	 * 
	 * @param message
	 *            A message to prompt the request.
	 * @return The input from the user or {@code null} when an error occurred.
	 * @throws UnsupportedOperationException
	 *             When requesting input is not supported.
	 */
	public String requestInput(String message) throws UnsupportedOperationException;

	/**
	 * Returns whether there is visual output available.
	 * 
	 * @return {@code true} if available, else {@code false}.
	 */
	public boolean hasVisualOutput();

	/**
	 * Returns the VisualOutput Object if available.
	 * 
	 * @return The VisualOutput Object.
	 * @throws UnsupportedOperationException
	 *             When VisualOutput is not available.
	 */
	public VisualOutput getVisualOutput() throws UnsupportedOperationException;

}
