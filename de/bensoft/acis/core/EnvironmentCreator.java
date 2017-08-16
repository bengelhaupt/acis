/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

import java.io.File;

import de.bensoft.acis.core.environment.Environment;
import de.bensoft.acis.core.environment.PackageEnvironment;
import de.bensoft.acis.core.environment.SystemEnvironment;
import de.bensoft.acis.core.environment.SystemProperties;
import de.bensoft.acis.core.environment.UserInfo;
import de.bensoft.acis.core.environment.VisualOutput;
import de.bensoft.acis.core.language.Language;
import de.bensoft.acis.utils.Preferences;

/**
 * Used by the system to create an {@link Environment} from the
 * {@link SystemEnvironment} and the {@link PackageEnvironment}.
 *
 */
class EnvironmentCreator {

	/**
	 * Creates an Environment from a {@link SystemEnvironment} and a
	 * {@link PackageEnvironment}
	 * 
	 * @param systemEnvironment
	 *            The {@link SystemEnvironment}.
	 * @param packageEnvironment
	 *            The specific {@link PackageEnvironment}.
	 * @return The merged Environment.
	 */
	static Environment createEnvironment(final SystemEnvironment systemEnvironment,
			final PackageEnvironment packageEnvironment) {

		return new Environment() {
			
			@Override
			public SystemProperties getSystemProperties() {
				return systemEnvironment.getSystemProperties();
			}

			@Override
			public UserInfo getUserInfo() {
				return systemEnvironment.getUserInfo();
			}

			@Override
			public boolean canSpeak() {
				return systemEnvironment.canSpeak();
			}

			@Override
			public void addOutput(String s) {
				systemEnvironment.addOutput(s);

			}

			@Override
			public void addWrittenOutput(String s) {
				systemEnvironment.addWrittenOutput(s);

			}

			@Override
			public void addSpokenOutput(String s) {
				systemEnvironment.addSpokenOutput(s);
			}
			

			@Override
			public boolean canRequestInput() {
				return systemEnvironment.canRequestInput();
			}

			@Override
			public String requestInput(String message) {
				return systemEnvironment.requestInput(message);
			}

			@Override
			public boolean hasVisualOutput() {
				return systemEnvironment.hasVisualOutput();
			}

			@Override
			public VisualOutput getVisualOutput() throws UnsupportedOperationException {
				return systemEnvironment.getVisualOutput();
			}

			@Override
			public File getDataDirectory() {
				return packageEnvironment.getDataDirectory();
			}

			@Override
			public Preferences getPreferences() {
				return packageEnvironment.getPreferences();
			}

			@Override
			public Language getLanguage() {
				return packageEnvironment.getLanguage();
			}

			@Override
			public ContextStack getContextStack() {
				return packageEnvironment.getContextStack();
			}
		};
	}
}