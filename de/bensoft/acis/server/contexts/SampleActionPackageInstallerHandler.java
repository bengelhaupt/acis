/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.server.contexts;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Map;

import de.bensoft.acis.core.ACIS;
import de.bensoft.acis.server.Server.HttpStatusCodeRepresentation;
import de.bensoft.acis.server.ServerContext;
import de.bensoft.acis.server.ServerContext.ServerContextHandler;
import de.bensoft.acis.server.ServerContext.ServerContextResult;

/**
 * Sample handler for installing ActionPackages from an url. <br>
 * Syntax: /&lt;CONTEXT_PATH&gt;?key=&lt;SESSION_TOKEN
 * (required)&amp;url=&lt;URL (required)&gt; <br>
 * Returns: &quot;ERROR: INCOMPLETE REQUEST DATA&quot; (400), &quot;ERROR: FILE
 * NOT FOUND&quot; (200), &quot;ERROR: INTERNAL SERVER ERROR (EXCEPTION)&quot;
 * (500) or the result if successful: &quot;true&quot;.
 */
public class SampleActionPackageInstallerHandler implements ServerContextHandler {

	private Runnable mPostInstall;

	public SampleActionPackageInstallerHandler(Runnable postInstall) {
		mPostInstall = postInstall;
	}

	@Override
	public ServerContext.ServerContextResult handle(ACIS system, Map<String, String> arguments) {
		if (arguments.containsKey("url")) {
			try {
				String url = arguments.get("url");
				URL fileUrl = new URL(url);
				String name = String.valueOf(System.currentTimeMillis());
				String[] p = fileUrl.getPath().split("/");
				String fName = p[p.length - 1];
				if (!fName.equals("") && !fName.equals(".") && !fName.equals(".."))
					name = fName;

				BufferedInputStream in = null;
				FileOutputStream fout = null;
				try {
					in = new BufferedInputStream(fileUrl.openStream());
					fout = new FileOutputStream(
							new File(system.getPackageFilesDirectory().getAbsolutePath() + "/" + name));

					final byte data[] = new byte[1024];
					int count;
					while ((count = in.read(data, 0, 1024)) != -1) {
						fout.write(data, 0, count);
					}

					new Thread(mPostInstall).start();

					return new ServerContextResult("true", HttpStatusCodeRepresentation.OK);
				} catch (Exception e) {
					return new ServerContextResult("ERROR: INTERNAL SERVER ERROR (" + e.toString() + ")",
							HttpStatusCodeRepresentation.INTERNAL_SERVER_ERROR);
				} finally {
					if (in != null) {
						in.close();
					}
					if (fout != null) {
						fout.close();
					}
				}
			} catch (Exception e) {
				return new ServerContextResult("ERROR: FILE NOT FOUND", HttpStatusCodeRepresentation.OK);
			}
		} else {
			return new ServerContextResult("ERROR: INCOMPLETE REQUEST DATA", HttpStatusCodeRepresentation.BAD_REQUEST);
		}
	}
}