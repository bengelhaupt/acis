/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.server.contexts;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.bensoft.acis.core.ACIS;
import de.bensoft.acis.core.Action;
import de.bensoft.acis.core.ActionResult;
import de.bensoft.acis.core.ActionResult.ActionResultCode;
import de.bensoft.acis.core.MatchResult;
import de.bensoft.acis.core.Parameter;
import de.bensoft.acis.core.WeightSet;
import de.bensoft.acis.core.environment.Environment;
import de.bensoft.acis.core.environment.SystemEnvironment;
import de.bensoft.acis.core.environment.SystemProperties;
import de.bensoft.acis.core.environment.UserInfo;
import de.bensoft.acis.core.environment.VisualOutput;
import de.bensoft.acis.core.language.Sentence;
import de.bensoft.acis.server.Server;
import de.bensoft.acis.server.ServerContext;
import de.bensoft.acis.server.ServerContext.ServerContextHandler;
import de.bensoft.acis.server.ServerContext.ServerContextResult;

/**
 * Sample handler for queries and input requests. <br>
 * Syntax: /&lt;CONTEXT_PATH&gt;?key=&lt;SESSION_TOKEN
 * (required)&gt;&amp;mode=&lt;request | respond (required; request: simple text
 * request; respond: respond to a input request)&gt; <br>
 * <ul>
 * <li>mode=request&amp;threshold=&lt;MATCHER_THRESHOLD (optional; between 0 and
 * 1)&gt;&amp;serveroutput=&lt;true | false (optional; whether there should be
 * output on the server, default is false)&gt;&amp;q=&lt;QUERY
 * (required)&gt;</li>
 * <li>mode=respond&amp;id=&lt;INPUT_REQUEST_ID
 * (required)&gt;&amp;content=&lt;REQUESTED_TEXT (required)&gt;</li>
 * </ul>
 * <br>
 * Returns: &quot;ERROR: INCOMPLETE REQUEST DATA&quot; (400), &quot;ERROR:
 * MALFORMED REQUEST DATA&quot; (400), &quot;ERROR: NO SUCH REQUEST ID&quot;
 * (400) or the result in XML-format: <br>
 * <ul>
 * <li>RESULT: &lt;response version=&quot;LIBRARY_VERSION&quot;&gt;
 * &lt;type&gt;RESULT&lt;/type&gt; &lt;name&gt;NAME&lt;/name&gt; &lt;result&gt;
 * &lt;code&gt;ACTIONRESULTCODE&lt;/code&gt;
 * &lt;message&gt;RESULT_MESSAGE&lt;/message&gt; &lt;/result&gt;
 * &lt;score&gt;MATCHER_SCORE&lt;/score&gt; &lt;output&gt; &lt;new&gt;
 * &lt;written&gt;NEW WRITTEN OUTPUT&lt;/written&gt; &lt;spoken&gt;NEW SPOKEN
 * OUTPUT&lt;/spoken&gt; &lt;/new&gt; &lt;total&gt; &lt;written&gt;TOTAL WRITTEN
 * OUTPUT&lt;/written&gt; &lt;spoken&gt;TOTAL SPOKEN OUTPUT&lt;/spoken&gt;
 * &lt;/total&gt; &lt;/output&gt; &lt;/response&gt;</li>
 * <li>NO_RESULTS: &lt;response version=&quot;LIBRARY_VERSION&quot;&gt;
 * &lt;type&gt;NO_RESULTS&lt;/type&gt; &lt;/response&gt;</li>
 * <li>REQUEST_INPUT: &lt;response version=&quot;LIBRARY_VERSION&quot;&gt;
 * &lt;type&gt;RESULT&lt;/type&gt; &lt;name&gt;NAME&lt;/name&gt; &lt;result&gt;
 * &lt;code&gt;ACTIONRESULTCODE&lt;/code&gt;
 * &lt;message&gt;RESULT_MESSAGE&lt;/message&gt; &lt;/result&gt;
 * &lt;score&gt;MATCHER_SCORE&lt;/score&gt; &lt;output&gt; &lt;new&gt;
 * &lt;written&gt;NEW WRITTEN OUTPUT&lt;/written&gt; &lt;spoken&gt;NEW SPOKEN
 * OUTPUT&lt;/spoken&gt; &lt;/new&gt; &lt;total&gt; &lt;written&gt;TOTAL WRITTEN
 * OUTPUT&lt;/written&gt; &lt;spoken&gt;TOTAL SPOKEN OUTPUT&lt;/spoken&gt;
 * &lt;/total&gt; &lt;/output&gt;
 * &lt;request&gt;&lt;id&gt;REQUEST_ID&lt;/id&gt;&lt;text&gt;REQUEST_PROMPT&lt;/text&gt;&lt;/request&gt;
 * &lt;/response&gt;</li>
 * </ul>
 */
public class SampleRequestHandler implements ServerContextHandler {

	private final String STANDARD_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private Map<String, String> mRequests = new HashMap<>();

	private String currentResponse;

	private String generateRequestId() {
		String reqId = "";
		char[] alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		Random rand = new Random();
		do {
			reqId = "";
			for (int i = 0; i < 16; i++) {
				reqId += alphabet[rand.nextInt(alphabet.length - 1)];
			}
		} while (reqId == "" || mRequests.containsKey(reqId));
		return reqId;
	}

	@Override
	public ServerContext.ServerContextResult handle(final ACIS system, Map<String, String> arguments) {
		final String currentRequestId = generateRequestId();
		currentResponse = "";

		if (arguments.containsKey("mode")) {
			String mode = arguments.get("mode");
			if (mode.equals("respond")) {
				if (arguments.containsKey("id") && arguments.containsKey("content")) {
					String requestId = arguments.get("id");
					if (mRequests.containsKey(requestId)) {
						mRequests.put(requestId, arguments.get("content"));
						// abort here
					} else {
						return new ServerContext.ServerContextResult("ERROR: NO SUCH REQUEST ID",
								Server.HttpStatusCodeRepresentation.BAD_REQUEST);
					}
				} else {
					return new ServerContext.ServerContextResult("ERROR: INCOMPLETE REQUEST DATA",
							Server.HttpStatusCodeRepresentation.BAD_REQUEST);
				}
			} else if (mode.equals("request")) {
				if (arguments.containsKey("q")) {
					String query = arguments.get("q");

					Float tres = 0f;
					if (arguments.containsKey("threshold"))
						tres = Float.valueOf(arguments.get("threshold"));

					Boolean serveroutput = false;
					if (arguments.containsKey("serveroutput"))
						serveroutput = Boolean.valueOf(arguments.get("serveroutput"));
					final boolean finalServeroutput = serveroutput;

					system.executeNewThread(query, tres, new WeightSet(), new ACIS.OnExecutionListener() {

						private float matchScore = 0f;

						@Override
						public MatchResult onGetBestResult(MatchResult[] results) {
							if (results.length == 0)
								currentResponse = STANDARD_RESPONSE + "<response version=\"" + ACIS.LIBRARY_VERSION
								+ "\"><type>NO_RESULTS</type></response>";
							MatchResult matchResult = super.onGetBestResult(results);
							if (matchResult != null)
								matchScore = matchResult.getScore();
							else
								matchScore = 0f;
							return matchResult;
						}

						@Override
						public ActionResult onActionRun(final Action action, Environment environment, Sentence sentence,
								Parameter[] parameter) {
							ServerEnvironment env = new ServerEnvironment() {
								String totalWritten = "";
								String totalSpoken = "";
								String partialWritten = "";
								String partialSpoken = "";

								@Override
								public SystemProperties getSystemProperties() {
									return system.getSystemEnvironment().getSystemProperties();
								}

								@Override
								public UserInfo getUserInfo() {
									return system.getSystemEnvironment().getUserInfo();
								}

								@Override
								public boolean canSpeak() {
									return true;
								}

								@Override
								public void addOutput(String s) {
									if (finalServeroutput) {
										system.getSystemEnvironment().addWrittenOutput(s);
										system.getSystemEnvironment().addSpokenOutput(s);
									}
									totalWritten += s + "\r\n";
									totalSpoken += s + "\r\n";
									partialWritten += s + "\r\n";
									partialSpoken += s + "\r\n";
								}

								@Override
								public void addWrittenOutput(String s) {
									if (finalServeroutput) {
										system.getSystemEnvironment().addWrittenOutput(s);
									}
									totalWritten += s + "\r\n";
									partialWritten += s + "\r\n";
								}

								@Override
								public void addSpokenOutput(String s) {
									if (finalServeroutput) {
										system.getSystemEnvironment().addSpokenOutput(s);
									}
									totalSpoken += s + "\r\n";
									partialSpoken += s + "\r\n";
								}

								@Override
								public boolean canRequestInput() {
									return true;
								}

								@Override
								public String requestInput(String s) throws UnsupportedOperationException {
									mRequests.put(currentRequestId, null);
									currentResponse = STANDARD_RESPONSE + "<response version=\"" + ACIS.LIBRARY_VERSION
											+ "\"><type>REQUEST_INPUT</type><name>" + action.getName()
											+ "</name><score>" + matchScore + "</score><output><new><written>"
											+ getPartialWrittenOutput() + "</written><spoken>"
											+ getPartialSpokenOutput() + "</spoken></new><total><written>"
											+ getTotalWrittenOutput() + "</written><spoken>" + getTotalSpokenOutput()
											+ "</spoken></total></output><request><id>" + currentRequestId
											+ "</id><text>" + s + "</text></request></response>";
									while (mRequests.get(currentRequestId) == null) {
										try {
											Thread.sleep(5);
										} catch (InterruptedException ignored) {
										}
									}
									resetPartialOutput();
									return mRequests.get(currentRequestId);
								}

								@Override
								public boolean hasVisualOutput() {
									return system.getSystemEnvironment().hasVisualOutput();
								}

								@Override
								public VisualOutput getVisualOutput() {
									return system.getSystemEnvironment().getVisualOutput();
								}

								@Override
								public String getTotalWrittenOutput() {
									return totalWritten;
								}

								@Override
								public String getTotalSpokenOutput() {
									return totalSpoken;
								}

								@Override
								public String getPartialWrittenOutput() {
									return partialWritten;
								}

								@Override
								public String getPartialSpokenOutput() {
									return partialSpoken;
								}

								@Override
								public void resetPartialOutput() {
									partialWritten = "";
									partialSpoken = "";
								}
							};

							ActionResult result;
							try {
								result = action.getActionMethod().run(system.getEnvironment(action.getPackage(), env),
										sentence, parameter);
							} catch (Exception e) {
								result = new ActionResult(ActionResultCode.INTERNAL_ERROR, e.toString());
							}

							currentResponse = STANDARD_RESPONSE + "<response version=\"" + ACIS.LIBRARY_VERSION
									+ "\"><type>RESULT</type><name>" + action.getName() + "</name><result><code>"
									+ result.getResultCode() + "</code><message>" + result.getMessage()
									+ "</message></result><score>" + matchScore + "</score><output><new><written>"
									+ env.getPartialWrittenOutput() + "</written><spoken>"
									+ env.getPartialSpokenOutput() + "</spoken></new><total><written>"
									+ env.getTotalWrittenOutput() + "</written><spoken>" + env.getTotalSpokenOutput()
									+ "</spoken></total></output></response>";
							mRequests.remove(currentRequestId);
							return result;
						}
					});
				} else {
					return new ServerContext.ServerContextResult("ERROR: INCOMPLETE REQUEST DATA",
							Server.HttpStatusCodeRepresentation.BAD_REQUEST);
				}
			} else {
				return new ServerContext.ServerContextResult("ERROR: MALFORMED REQUEST DATA",
						Server.HttpStatusCodeRepresentation.BAD_REQUEST);
			}
		} else {
			return new ServerContext.ServerContextResult("ERROR: INCOMPLETE REQUEST DATA",
					Server.HttpStatusCodeRepresentation.BAD_REQUEST);
		}

		while (currentResponse.equals("")) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException ignored) {
			}
		}
		ServerContextResult scr = new ServerContextResult(currentResponse, Server.HttpStatusCodeRepresentation.OK);
		scr.setContentType("text/xml");
		return scr;
	}

	private interface ServerEnvironment extends SystemEnvironment {
		String getTotalWrittenOutput();

		String getTotalSpokenOutput();

		String getPartialWrittenOutput();

		String getPartialSpokenOutput();

		void resetPartialOutput();
	}
}
