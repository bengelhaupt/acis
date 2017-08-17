/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.bensoft.acis.utils.IOUtils.SavingConfig;

/**
 * Contains classes that can be used for logging purposes.
 *
 */
public class Logging {

	/**
	 * Represents a logger.<br>
	 * There are three log levels supported:
	 * <ul>
	 * <li>INFO (i)</li>
	 * <li>WARN (w)</li>
	 * <li>ERROR (e)</li>
	 * </ul>
	 * <br>
	 * A log entry looks like this: &lt;I/W/E&gt;/dd.MM.yyyy HH:mm:ss |
	 * &lt;LOGTAG&gt; (&lt;LOGMESSAGE&gt;)
	 *
	 */
	public static class Logger {

		private LoggingConfig loggingConfig;

		/**
		 * Creates a new Logger using a {@link LoggingConfig}.
		 * 
		 * @param config
		 *            The {@link LoggingConfig} to use.
		 */
		public Logger(LoggingConfig config) {
			loggingConfig = config;
		}

		/**
		 * Sets the {@link LoggingConfig}.
		 * 
		 * @param config
		 *            The {@link LoggingConfig} to set.
		 */
		public void setLoggingConfig(LoggingConfig config) {
			loggingConfig = config;
		}

		/**
		 * Returns the used {@link LoggingConfig}.
		 * 
		 * @return The {@link LoggingConfig}.
		 */
		public LoggingConfig getLoggingConfig() {
			return loggingConfig;
		}

		/**
		 * Deletes the log File as specified in the {@link LoggingConfig}.
		 * 
		 * @return {@code true} if and only if the file is successfully deleted,
		 *         otherwise {@code false}.
		 */
		public boolean delete() {
			return loggingConfig.getFile().delete();
		}

		/**
		 * Adds a new log entry with log level INFO to the log file. The
		 * shortcut is 'I'.<br>
		 * This only logs if logging is enabled in the {@link LoggingConfig}.
		 * 
		 * @param tag
		 *            The log tag to use. Should represent the application's
		 *            name and/or a component of it.
		 * @param msg
		 *            The log message.
		 */
		public void i(String tag, String msg) {
			if (loggingConfig.isEnabled())
				try {
					Writer fw = new OutputStreamWriter(new FileOutputStream(loggingConfig.getFile(), true),
							StandardCharsets.UTF_8);
					String logstr = "I/" + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY)
							.format(Calendar.getInstance().getTime()) + " | " + tag + " (" + msg + ")";
					fw.append("\r\n" + logstr);
					fw.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
		}

		/**
		 * Adds a new log entry with log level WARN to the log file. The
		 * shortcut is 'W'.<br>
		 * This only logs if logging is enabled in the {@link LoggingConfig}.
		 * 
		 * @param tag
		 *            The log tag to use. Should represent the application's
		 *            name and/or a component of it.
		 * @param msg
		 *            The log message.
		 */
		public void w(String tag, String msg) {
			if (loggingConfig.isEnabled())
				try {
					Writer fw = new OutputStreamWriter(new FileOutputStream(loggingConfig.getFile(), true),
							StandardCharsets.UTF_8);
					String logstr = "W/" + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY)
							.format(Calendar.getInstance().getTime()) + " | " + tag + " (" + msg + ")";
					fw.append("\r\n" + logstr);
					fw.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
		}

		/**
		 * Adds a new log entry with log level ERROR to the log file. The
		 * shortcut is 'E'.<br>
		 * This only logs if logging is enabled in the {@link LoggingConfig}.
		 * 
		 * @param tag
		 *            The log tag to use. Should represent the application's
		 *            name and/or a component of it.
		 * @param msg
		 *            The log message.
		 */
		public void e(String tag, String msg) {
			if (loggingConfig.isEnabled())
				try {
					Writer fw = new OutputStreamWriter(new FileOutputStream(loggingConfig.getFile(), true),
							StandardCharsets.UTF_8);
					String logstr = "E/" + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY)
							.format(Calendar.getInstance().getTime()) + " | " + tag + " (" + msg + ")";
					fw.append("\r\n" + logstr);
					fw.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
		}

	}

	/**
	 * Represents the way to save a logging file.
	 * 
	 * @see IOUtils.SavingConfig
	 *
	 */
	public static class LoggingConfig extends SavingConfig {

		private boolean enabled = true;

		/**
		 * Enables or disables logging to the file.
		 * 
		 * @param e
		 *            {@code true} to enable, {@code false} to disable.
		 */
		public void setEnabled(boolean e) {
			enabled = e;
		}

		/**
		 * Returns whether logging is enabled.
		 * 
		 * @return {@code true} when enabled, else {@code false}.
		 */
		public boolean isEnabled() {
			return enabled;
		}

		/**
		 * Creates a new LoggingConfig.<br>
		 * 
		 * @see SavingConfig
		 * 
		 * @param f
		 *            The File to log to.
		 * @throws IOException
		 *             When {@code f} is not a file but a directory.
		 */
		public LoggingConfig(File f) throws IOException {
			super(f);
		}
	}

	/**
	 * Represents a class other classes can implement to gain logging
	 * functionality.
	 *
	 */
	public static class Loggable {

		private Logger mLogger;

		/**
		 * Gets the Logger.
		 * 
		 * @return The Logger. If the Logger was not set previously, a new one
		 *         with the file "./default.log" is created. Returns
		 *         {@code null} when there is an IOException creating a Logger.
		 */
		public Logger getLogger() {
			if (mLogger != null)
				return mLogger;
			try {
				return new Logger(new LoggingConfig(new File("./default.log")));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * Sets the Logger.
		 * 
		 * @param logger
		 *            The Logger to set.
		 */
		public void setLogger(Logger logger) {
			mLogger = logger;
		}
	}
}
