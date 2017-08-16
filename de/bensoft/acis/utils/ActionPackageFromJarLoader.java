/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.utils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.bensoft.acis.core.ACIS;
import de.bensoft.acis.core.Action;
import de.bensoft.acis.core.ActionMalformedException;
import de.bensoft.acis.core.ActionPackage;

/**
 * Helps loading Actions for the ACIS system from classes in a jar-package.
 * <p>
 * Note: This works only on systems which support direct class loading. For
 * example Android (which uses the ART/Dalvik runtime) needs an additional
 * <i>classes.dex</i> file in the package. You can create such a
 * <i>classes.dex</i> file using dx from the Android build-tools: <i>dx --dex
 * &lt;INPUT_JAR&gt; --output &lt;OUTPUT_CLASSES.DEX&gt;</i>
 *
 */
public class ActionPackageFromJarLoader {

	private static final String LOG_TAG = "ACTION_JAR_LOADER";

	/**
	 * Loads Actions defined in a jar-package into the ACIS system.
	 * 
	 * @param path
	 *            The filepath to the package (extension '.jar' not mandatory).
	 * @param system
	 *            The system to load the Actions in.
	 * @throws Exception
	 *             When there is an error with the file.
	 */
	public static void loadFromJar(String path, ACIS system) throws Exception {
		JarFile jarFile = new JarFile(path);
		Enumeration<JarEntry> en = jarFile.entries();

		URL[] urls = { new URL("jar:file:" + path + "!/") };
		URLClassLoader cl = URLClassLoader.newInstance(urls);

		try {
			while (en.hasMoreElements()) {
				JarEntry je = en.nextElement();
				if (je.isDirectory() || !je.getName().endsWith(".class")) {
					continue;
				}
				String className = je.getName().substring(0, je.getName().length() - 6);
				className = className.replace('/', '.');
				Class<?> c = cl.loadClass(className);
				for (Class<?> interf : c.getInterfaces()) {
					if (interf.getName().equals("de.bensoft.acis.core.ActionPackage")) {
						try {
							ActionPackage ap = (ActionPackage) c.newInstance();
							try {
								Action[] a = ap.getActions(system.getLanguage());
								system.getActionManager().add(a);
							} catch (ActionMalformedException ie) {
								system.getLogger().e(LOG_TAG, "There was an error while creating Action '"
										+ ie.getActionName() + "': " + ie.toString());
							}
						} catch (InstantiationException i) {
							system.getLogger().e(LOG_TAG, "There was an error while instantiating Class '" + c.getName()
									+ "': " + i.toString());
						}
					}
				}
			}
			jarFile.close();
			cl.close();
		} catch (Exception e) {
			system.getLogger().e(LOG_TAG,
					"There was an error while loading Actions from jar '" + path + "': " + e.toString());
			system.getLogger().w(LOG_TAG,
					"Maybe your system does not support loading .class files from a .jar file. (Such as Android only loads classes from .dex files)");
		}
	}
}