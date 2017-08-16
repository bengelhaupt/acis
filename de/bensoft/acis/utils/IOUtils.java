/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Provides common utilities to help with handling IO such as Files or ways to
 * save things.
 *
 */
public class IOUtils {

	/**
	 * Represents a way to save to a file.<br>
	 * In difference to simply using a File Object, this class checks if it is
	 * really a file and not a directory. In addition to that, the file is
	 * actually created at instantiation of this Object and eventual parent
	 * directories are automatically created.
	 *
	 */
	public static class SavingConfig {

		private File mFile;

		/**
		 * Sets the underlying {@link java.io.File}.
		 * 
		 * @param f
		 *            The {@link java.io.File} to set.
		 */
		public void setFile(File f) {
			mFile = f;
		}

		/**
		 * Returns the underlying {@link java.io.File} Object.
		 * 
		 * @return The {@link java.io.File}.
		 */
		public File getFile() {
			return mFile;
		}

		/**
		 * Creates a new SavingConfig Object. If the {@link java.io.File} does
		 * not exist, it will be created. Any non-existing parent directories
		 * are created too.
		 * 
		 * @param f
		 *            The {@link java.io.File} to save to.
		 * @throws IOException
		 *             When {@code f} is not a file but a directory.
		 */
		public SavingConfig(File f) throws IOException {
			if (f.isDirectory())
				throw new IOException("Specified java.io.File is a directory, not a file.");

			mFile = f;

			if (!mFile.exists()) {
				try {
					mFile.getParentFile().mkdirs();
					mFile.createNewFile();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Reads the content of a {@link java.io.File} into a String.<br>
	 * Line-breaks occurring in the {@link java.io.File} are also in the output
	 * String.
	 * 
	 * @param file
	 *            The {@link java.io.File} to read the content of.
	 * @return The content String.
	 */
	public static String readFromFile(File file) {
		try {
			InputStream inputStream = new FileInputStream(file);
			BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			StringBuilder total = new StringBuilder();
			String l;
			while ((l = r.readLine()) != null) {
				total.append(l + System.lineSeparator());
			}
			r.close();

			String f = total.toString();
			return f;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Reads the content of a {@link java.io.File} into a String.<br>
	 * Line-breaks occurring in the {@link java.io.File} will not be present in
	 * the output String.
	 * 
	 * @param file
	 *            The {@link java.io.File} to read the content of.
	 * @return The content String without any line-breaks.
	 */
	public static String readSingleLineFromFile(File file) {
		try {
			InputStream inputStream = new FileInputStream(file);
			BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			StringBuilder total = new StringBuilder();
			String l;
			while ((l = r.readLine()) != null) {
				total.append(l);
			}
			r.close();

			String f = total.toString();
			return f;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks whether a host is reachable.
	 * 
	 * @param address
	 *            The host's address to check for. If it starts with a protocol
	 *            identifier, it is split at "://" and the second part is used
	 *            for checking.
	 * @param timeoutMillis
	 *            The timeout milliseconds after which a connection attempt is
	 *            considered as failed.
	 * @return {@code true} when the host is reachable. Returns {@code false}
	 *         when the host is not reachable within the {@code timeoutMillis}
	 *         or an Exception occurred.
	 */
	public static boolean isHostReachable(String address, int timeoutMillis) {
		if (address == null || address.equals(""))
			return false;
		try {
			if (address.split("://").length > 0)
				address = address.split("://")[1];
			return InetAddress.getByName(address).isReachable(timeoutMillis);
		} catch (IOException ex) {
			return false;
		}
	}
}