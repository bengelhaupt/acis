/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.server.contexts;

import java.io.File;
import java.util.Map;

import de.bensoft.acis.core.ACIS;
import de.bensoft.acis.server.Server.HttpStatusCodeRepresentation;
import de.bensoft.acis.server.ServerContext;
import de.bensoft.acis.server.ServerContext.ServerContextHandler;
import de.bensoft.acis.server.ServerContext.ServerContextResult;
import de.bensoft.acis.utils.IOUtils;

/**
 * Sample handler for showing files. The path required is the relative path to
 * the data directory.<br>
 * Syntax: /&lt;CONTEXT_PATH&gt;?key=&lt;SESSION_TOKEN
 * (required)&amp;path=&lt;PATH_TO_FILE (required)&gt;<br>
 * Returns: &quot;ERROR: INCOMPLETE REQUEST DATA&quot; (400), &quot;ERROR: FILE
 * DOES NOT EXIST&quot; (404), &quot;ERROR: INTERNAL SERVER ERROR
 * (EXCEPTION)&quot; (500) or the result in XML-format: <br>
 * &lt;response version=&quot;LIBRARY_VERSION&quot;&gt; &lt;package
 * name=&quot;PACKAGE_NAME&quot; description=&quot;PACKAGE_DESCRIPTION&quot;
 * minversion=&quot;MINIMAL_LIBRARY_VERSION&quot;&gt; &lt;action
 * name=&quot;ACTION_NAME&quot; visibility=&quot;CONTEXT_VISIBILITY&quot;
 * trigger=&quot;TRIGGER&quot;/&gt; &lt;action name=&quot;ACTION_NAME&quot;
 * visibility=&quot;CONTEXT_VISIBILITY&quot; trigger=&quot;TRIGGER&quot;/&gt;
 * &lt;/package&gt; &lt;/response&gt;
 */
public class SampleFileViewHandler implements ServerContextHandler {

	private final String STANDARD_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	@Override
	public ServerContext.ServerContextResult handle(ACIS system, Map<String, String> arguments) {
		if (arguments.containsKey("path")) {
			File f = new File(system.getDataDirectory() + arguments.get("path"));
			if (f.exists() && !f.isDirectory()) {
				String response = STANDARD_RESPONSE + "<response version=\"" + ACIS.LIBRARY_VERSION + "\">";
				response += encode(IOUtils.readFromFile(f));
				response += "</response>";
				ServerContextResult result = new ServerContextResult(response, HttpStatusCodeRepresentation.OK);
				result.setContentType("text/xml");
				return result;
			} else {
				return new ServerContextResult("ERROR: FILE DOES NOT EXIST", HttpStatusCodeRepresentation.NOT_FOUND);
			}
		} else {
			return new ServerContextResult("ERROR: INCOMPLETE REQUEST DATA", HttpStatusCodeRepresentation.BAD_REQUEST);
		}
	}

	private String encode(String string) {
		StringBuilder escapedTxt = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char tmp = string.charAt(i);
			switch (tmp) {
			case '<':
				escapedTxt.append("&lt;");
				break;
			case '>':
				escapedTxt.append("&gt;");
				break;
			case '&':
				escapedTxt.append("&amp;");
				break;
			case '"':
				escapedTxt.append("&quot;");
				break;
			case '\'':
				escapedTxt.append("&#x27;");
				break;
			case '/':
				escapedTxt.append("&#x2F;");
				break;
			default:
				escapedTxt.append(tmp);
			}
		}
		return escapedTxt.toString();
	}
}