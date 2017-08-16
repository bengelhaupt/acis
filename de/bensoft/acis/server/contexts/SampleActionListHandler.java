/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.server.contexts;

import java.util.Map;

import de.bensoft.acis.core.ACIS;
import de.bensoft.acis.core.Action;
import de.bensoft.acis.core.ActionPackage;
import de.bensoft.acis.server.Server.HttpStatusCodeRepresentation;
import de.bensoft.acis.server.ServerContext;
import de.bensoft.acis.server.ServerContext.ServerContextHandler;
import de.bensoft.acis.server.ServerContext.ServerContextResult;

/**
 * Sample handler for listing Actions and ActionPackages. <br>
 * Syntax: /&lt;CONTEXT_PATH&gt;?key=&lt;SESSION_TOKEN (required) <br>
 * Returns: &quot;ERROR: INTERNAL SERVER ERROR (EXCEPTION)&quot; (500) or the
 * result in XML-format: <br>
 * &lt;response version=&quot;LIBRARY_VERSION&quot;&gt; &lt;package
 * name=&quot;PACKAGE_NAME&quot; description=&quot;PACKAGE_DESCRIPTION&quot;
 * minversion=&quot;MINIMAL_LIBRARY_VERSION&quot;&gt; &lt;action
 * name=&quot;ACTION_NAME&quot; visibility=&quot;CONTEXT_VISIBILITY&quot;
 * trigger=&quot;TRIGGER&quot;/&gt; &lt;action name=&quot;ACTION_NAME&quot;
 * visibility=&quot;CONTEXT_VISIBILITY&quot; trigger=&quot;TRIGGER&quot;/&gt;
 * &lt;/package&gt; &lt;/response&gt;
 */
public class SampleActionListHandler implements ServerContextHandler {

	private final String STANDARD_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	@Override
	public ServerContext.ServerContextResult handle(ACIS system, Map<String, String> arguments) {
		String response = STANDARD_RESPONSE + "<response version=\"" + ACIS.LIBRARY_VERSION + "\">";
		try {
			for (ActionPackage pack : system.getActionManager().getActionPackages()) {
				response += "<package name=\"" + encode(pack.getName()) + "\" description=\""
						+ encode(pack.getDescription()) + "\" minversion=\"" + pack.getMinimumRequiredLibraryVersion()
						+ "\">";
				for (Action a : pack.getActions(system.getLanguage()))
					response += "<action name=\"" + encode(a.getName().replace(pack.getName() + "/", ""))
							+ "\" visibility=\"" + encode(a.getContextVisibility().toString().toLowerCase())
							+ "\" trigger=\"" + encode(a.getTrigger()) + "\"></action>";
				response += "</package>";
			}
			response += "</response>";
		} catch (Exception e) {
			response = "ERROR: INTERNAL SERVER ERROR (" + e.toString() + ")";
			return new ServerContextResult(response, HttpStatusCodeRepresentation.INTERNAL_SERVER_ERROR);
		}
		ServerContextResult result = new ServerContextResult(response, HttpStatusCodeRepresentation.OK);
		result.setContentType("text/xml");
		return result;
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