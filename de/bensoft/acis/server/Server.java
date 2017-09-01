/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.bensoft.acis.core.ACIS;
import de.bensoft.acis.server.ServerContext.ServerContextHandler;
import de.bensoft.acis.server.ServerContext.ServerContextResult;
import de.bensoft.acis.utils.Logging.Loggable;

/**
 * Represents a web server specially designed for an ACIS system.<br>
 * It manages {@link User}s and their {@link Session}s.<br>
 * Functionality can be added through {@link ServerContext}s and their
 * corresponding {@link ServerContextHandler}s such as the
 * {@link de.bensoft.acis.server.contexts.SampleRequestHandler}.<br>
 * <br>
 * Request syntax: e.g. http://myacis.com:4964/<br>
 * <p>
 * Authentication:
 * /auth?user=&lt;USERNAME&gt;&amp;pass=&lt;PASSWORD_AS_MD5&gt;<br>
 * Returns: The session token or &quot;ERROR: INCORRECT AUTHENTICATION
 * DATA&quot; (401), &quot;ERROR: INCOMPLETE AUTHENTICATION DATA&quot; (400)
 * </p>
 * <p>
 * Logout: /deauth?key=&lt;SESSION_TOKEN&gt;<br>
 * Returns: true (if successful) or &quot;ERROR: INTERNAL SERVER ERROR&quot;
 * (500)
 * </p>
 * <p>
 * {@link ServerContext}s added through {@link #registerContext(ServerContext)}:
 * /&lt;CONTEXT_PATH&gt;?key=&lt;SESSION_TOKEN&gt; + PARAMS (key only if context
 * requires authentication)<br>
 * Returns standardized: &quot;ERROR: AUTHENTICATION REQUIRED&quot; (401),
 * &quot;ERROR: SESSION NOT FOUND OR EXPIRED&quot; (401), &quot;ERROR: USER NOT
 * ALLOWED TO ACCESS THIS PATH&quot; (403), &quot;ERROR: INTERNAL SERVER
 * ERROR&quot; (500)
 * </p>
 */
public class Server extends Loggable implements Runnable {

	private static final String LOG_TAG = "SERVER";

	private final int mPort;
	private boolean mIsRunning;
	private ServerSocket mServerSocket;
	private ArrayList<ServerContext> mServerContexts = new ArrayList<ServerContext>(0);

	private long mTokenExpirationTime = 3600000; // 1 hour
	private int mTokenLength = 32;

	private List<Session> mSessions = new ArrayList<Session>(0);

	private ArrayList<User> mUsers = new ArrayList<User>(0);

	/**
	 * Constructor for the server.
	 * 
	 * @param port
	 *            The port the server will run at.
	 */
	public Server(int port) {
		mPort = port;

		// register basic authentication contexts
		this.registerContext(new ServerContext("/auth", null, new ServerContextHandler() {

			@Override
			public ServerContextResult handle(ACIS system, Map<String, String> arguments) {
				String response = "";
				String statuscode = HttpStatusCodeRepresentation.INTERNAL_SERVER_ERROR;
				if (arguments.containsKey("user") && arguments.containsKey("pass")) {
					String user = arguments.get("user");
					String pass = arguments.get("pass");

					if (checkCredentials(user, pass)) {
						statuscode = HttpStatusCodeRepresentation.OK;

						String sessionid = generateSessionId(mTokenLength);
						addSession(sessionid, user);

						response = sessionid;
					} else {
						statuscode = HttpStatusCodeRepresentation.UNAUTHORIZED;
						response = "ERROR: INCORRECT AUTHENTICATION DATA";
					}
				} else {
					response = "ERROR: INCOMPLETE AUTHENTICATION DATA";
					statuscode = HttpStatusCodeRepresentation.BAD_REQUEST;
				}

				return new ServerContextResult(response, statuscode);
			}

		}, false));
		this.registerContext(new ServerContext("/deauth", null, new ServerContextHandler() {

			@Override
			public ServerContextResult handle(ACIS system, Map<String, String> arguments) {
				String response = "ERROR: INTERNAL SERVER ERROR";
				String statuscode = HttpStatusCodeRepresentation.INTERNAL_SERVER_ERROR;
				if (arguments.containsKey("key")) {
					String sid = arguments.get("key");
					removeSession(sid);
					response = "true";
					statuscode = HttpStatusCodeRepresentation.OK;
				}
				return new ServerContextResult(response, statuscode);
			}

		}, true));
	}

	/**
	 * Returns the expiration time of a token.
	 * 
	 * @return The token expiration time.
	 */
	public long getTokenExpirationTime() {
		return mTokenExpirationTime;
	}

	/**
	 * Sets the expiration time of a token.
	 * 
	 * @param tokenExpirationTime
	 *            The token expiration time to set.
	 */
	public void setTokenExpirationTime(long tokenExpirationTime) {
		this.mTokenExpirationTime = tokenExpirationTime;
	}

	/**
	 * Returns the token length.
	 * 
	 * @return The token length.
	 */
	public int getTokenLength() {
		return mTokenLength;
	}

	/**
	 * Sets the token length.
	 * 
	 * @param tokenLength
	 *            The token length to set.
	 */
	public void setTokenLength(int tokenLength) {
		this.mTokenLength = tokenLength;
	}

	/**
	 * Registers a new {@link ServerContext} in the server.
	 * 
	 * @param context
	 *            The {@link ServerContext} to register.
	 * @throws IllegalArgumentException
	 *             When there is already a registered {@link ServerContext} with
	 *             the same route as {@code context}.
	 */
	public void registerContext(ServerContext context) throws IllegalArgumentException {
		for (ServerContext c : mServerContexts) {
			if (c.getRoute().equals(context.getRoute()))
				throw new IllegalArgumentException("There is already a ServerContext with that route.");
		}
		mServerContexts.add(context);
	}

	/**
	 * Starts the web server listening on the specified port.
	 *
	 * @throws Exception
	 *             When the specified port is already in use.
	 */
	public void start() throws Exception {
		try {
			mServerSocket = null;
			try {
				mServerSocket = new ServerSocket(mPort);
			} catch (BindException e) {
				getLogger().i(LOG_TAG,
						"Server could not be started: Port " + String.valueOf(mPort) + " already in use.");
				throw new Exception(
						"Server could not be started because port " + String.valueOf(mPort) + " is already in use.");
			} finally {
				if (mServerSocket != null)
					try {
						mServerSocket.close();
					} catch (IOException ignored) {
					}
			}

			if (mUsers.size() == 0)
				getLogger().w(LOG_TAG, "No credentials set.");
			getLogger().i(LOG_TAG, "Server successfully started on port " + String.valueOf(mPort) + ".");
			mIsRunning = true;
			new Thread(this).start();
		} catch (IOException e) {
			getLogger().e(LOG_TAG, "Server could not be started: " + e.toString());
			throw new Exception("Server could not be started.", e);
		}
	}

	/**
	 * Stops the web server.
	 */
	public void stop() {
		try {
			mIsRunning = false;
			if (null != mServerSocket) {
				mServerSocket.close();
				mServerSocket = null;
				getLogger().i(LOG_TAG, "Server stopped");
			}
		} catch (IOException e) {
			getLogger().e(LOG_TAG, "There was an error stopping the server: " + e.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			mServerSocket = new ServerSocket(mPort);
			while (mIsRunning) {
				final Socket socket = mServerSocket.accept();
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							handle(socket);
							socket.close();
						} catch (IOException e) {
							getLogger().e(LOG_TAG, "There was an error handling a request: " + e.toString());
						}
					}
				}).start();
			}
		} catch (SocketException e) {
			getLogger().e(LOG_TAG, "There was an error handling a request: " + e.toString());
		} catch (IOException e) {
			getLogger().e(LOG_TAG, "There was an error handling a request: " + e.toString());
		}
	}

	/**
	 * Respond to a request from a client.
	 *
	 * @param socket
	 *            The client socket.
	 */
	private void handle(Socket socket) {
		BufferedReader reader = null;
		PrintStream output = null;
		try {
			String route = "";

			// Read HTTP headers and parse out the route.
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			String line;
			while (!isEmpty(line = reader.readLine())) {
				if (line.startsWith("GET /")) {
					int start = line.indexOf('/');
					int end = line.indexOf(' ', start);
					route = line.substring(start, end);
					break;
				}
			}

			// Output stream that we send the response to
			output = new PrintStream(socket.getOutputStream(), true);

			// Set initial status code to 404 and bytes to empty array
			ServerContextResult result = new ServerContextResult("", HttpStatusCodeRepresentation.NOT_FOUND);

			String[] splittedRoute = route.split("\\?");
			ServerContext context = getContextByRoute(splittedRoute[0]);
			if (context != null) {
				Map<String, String> args = parseGETArguments("");
				if (splittedRoute.length > 1)
					args = parseGETArguments(splittedRoute[1]);

				if (context.requiresAuthentication()) {
					if (args.containsKey("key")) {
						String sid = args.get("key");

						if (isSessionGenuine(sid)) {

							if (!isUserRestricted(sid, splittedRoute[0])) {
								result = context.getContextHandler().handle(context.getSystem(), args);
							} else {
								result = new ServerContextResult("ERROR: USER NOT ALLOWED TO ACCESS THIS PATH",
										HttpStatusCodeRepresentation.FORBIDDEN);
							}
						} else {
							result = new ServerContextResult("ERROR: SESSION NOT FOUND OR EXPIRED",
									HttpStatusCodeRepresentation.UNAUTHORIZED);
						}
					} else {
						result = new ServerContextResult("ERROR: AUTHENTICATION REQUIRED",
								HttpStatusCodeRepresentation.UNAUTHORIZED);
					}

				} else {
					result = context.getContextHandler().handle(context.getSystem(), args);
				}
			}

			byte[] data = result.getResponse().getBytes(StandardCharsets.UTF_8);
			// Send out the content.
			output.println("HTTP/1.0 " + result.getStatusCode());
			output.println("Content-Type: " + result.getContentType());
			output.println("Content-Length: " + data.length);
			output.println("Access-Control-Allow-Origin: *");
			output.println();
			output.write(data);
			output.flush();
		} catch (Exception e) {
			getLogger().e(LOG_TAG, "There was an error handling a request: " + e.toString());
		} finally {
			if (null != output) {
				output.close();
			}
			if (null != reader) {
				try {
					reader.close();
				} catch (Exception ignored) {
				}
			}
		}
	}

	/**
	 * Adds a {@link User} to the system.
	 * 
	 * @param user
	 *            The {@link User} to add.
	 */
	public void addUser(User user) {
		mUsers.add(user);
	}

	/**
	 * Returns a {@link User} object.
	 * 
	 * @param username
	 *            The name of the {@link User}.
	 * @return The {@link User} if it is present, else returns {@code null}.
	 */
	public User getUser(String username) {
		for (User u : mUsers) {
			if (u.getName().toLowerCase().equals(username))
				return u;
		}
		return null;
	}

	/**
	 * Removes a certain {@link User} from the system.
	 * 
	 * @param username
	 *            The {@link User}'s name to remove.
	 */
	public void removeUser(String username) {
		for (int i = 0; i < mUsers.size(); i++) {
			if (mUsers.get(i).getName().equals(username))
				mUsers.remove(i);
		}
	}

	/**
	 * Checks whether {@link User} and {@code passphrase} are valid.
	 * 
	 * @param user
	 *            The {@link User}'s name.
	 * @param passphrase
	 *            The password.
	 * @return {@code true} when the credentials are correct, else
	 *         {@code false}.
	 */
	private boolean checkCredentials(String user, String passphrase) {
		User get = getUser(user.toLowerCase());
		if (get != null)
			if (generateMD5(get.getPassword()).equals(passphrase))
				return true;
		return false;
	}

	/**
	 * Checks whether a owner ({@link User}) of a session ID is restricted for a
	 * given path.
	 * 
	 * @param sid
	 *            The session Id.
	 * @param path
	 *            The server path to check for.
	 * @return {@code true} when the {@link User} is restricted, else
	 *         {@code false}.
	 */
	private boolean isUserRestricted(String sid, String path) {
		if (isSessionGenuine(sid)) {
			for (String p : getUser(getSession(sid).getUsername()).getRestrictedPaths()) {
				if (p.equals(path))
					return true;
			}
		} else {
			return true;
		}
		return false;
	}

	/**
	 * Generates a unique session ID String.
	 * 
	 * @param len
	 *            The length of the session ID.
	 * @return The session id.
	 */
	public String generateSessionId(int len) {
		String sessid = "";
		char[] alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		Random r = new Random();

		do {
			sessid = "";
			for (int i = 0; i < len; i++) {
				sessid += alphabet[r.nextInt(alphabet.length - 1)];
			}
		} while (sessid == "" || getSession(sessid) != null);

		return sessid;
	}

	/**
	 * Returns a {@link Session} object.
	 * 
	 * @param sid
	 *            The session ID of the {@link Session} to return.
	 * @return {@code true} if the session ID is present, else returns
	 *         {@code null}.
	 */
	private Session getSession(String sid) {
		for (int i = 0; i < mSessions.size(); i++) {
			if (mSessions.get(i).getId().equals(sid))
				return mSessions.get(i);
		}
		return null;
	}

	/**
	 * Checks whether a session is expired.
	 * 
	 * @param sid
	 *            The session ID to check.
	 * @return {@code true} if session is expired, else {@code false}.
	 */
	private boolean isSessionExpired(String sid) {
		return (Calendar.getInstance().getTimeInMillis() - getSession(sid).getTimeCreated()) > mTokenExpirationTime;
	}

	/**
	 * Adds a new session to the system.
	 * 
	 * @param sid
	 *            The new session ID.
	 * @param username
	 *            The name of the {@link User} of the new {@link Session}.
	 */
	private void addSession(String sid, String username) {
		mSessions.add(new Session(sid, Calendar.getInstance().getTimeInMillis(), username));
	}

	/**
	 * Removes a session from the system.
	 * 
	 * @param sid
	 *            The session ID of the session.
	 */
	private void removeSession(String sid) {
		for (int i = 0; i < mSessions.size(); i++) {
			if (mSessions.get(i).getId().equals(sid))
				mSessions.remove(i);
		}
	}

	/**
	 * Checks whether a session is genuine (valid). This also checks whether it
	 * is expired and returns {@code false} when this is the case.
	 * 
	 * @param sid
	 *            The session ID to check.
	 * @return {@code true} if the session is valid, else {@code false}.
	 */
	private boolean isSessionGenuine(String sid) {
		if (getSession(sid) != null)
			if (!isSessionExpired(sid))
				return true;
		return false;
	}

	/**
	 * Generates a MD5 hash from a String.
	 * 
	 * @param str
	 *            The input String.
	 * @return The MD5 hashed String.
	 */
	private String generateMD5(String str) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(StandardCharsets.UTF_8.encode(str));
			return String.format("%032x", new BigInteger(1, md5.digest()));
		} catch (NoSuchAlgorithmException ignored) {
		}
		return null;
	}

	/**
	 * Parses the arguments of the given {@code url}.
	 *
	 * @param query
	 *            The query part of the url.
	 * @return A Map with the GET arguments.
	 */
	private Map<String, String> parseGETArguments(String query) {
		Map<String, String> result = new HashMap<String, String>(0);
		try {
			for (String param : query.split("&")) {
				String pair[] = param.split("=");
				if (pair.length > 1) {
					result.put(java.net.URLDecoder.decode(pair[0], "UTF-8"),
							java.net.URLDecoder.decode(pair[1], "UTF-8"));
				} else {
					result.put(java.net.URLDecoder.decode(pair[0], "UTF-8"), "");
				}
			}
		} catch (UnsupportedEncodingException e) {
			getLogger().e(LOG_TAG, "There was an error parsing the request GET arguments: " + e.toString());
		}

		return result;
	}

	/**
	 * Gets the {@link ServerContext} for a specific route.
	 * 
	 * @param route
	 *            The route of the {@link ServerContext} to search (without
	 *            query).
	 * @return The {@link ServerContext} for the route. {@code null} if there is
	 *         no such {@link ServerContext}.
	 */
	private ServerContext getContextByRoute(String route) {
		for (ServerContext c : mServerContexts) {
			if (c.getRoute().equals(route))
				return c;
		}
		return null;
	}

	/**
	 * Checks whether a String is {@code null} or empty ("").
	 * 
	 * @param s
	 *            The input String.
	 * @return {@code true} when it is empty, else {@code false}.
	 */
	private boolean isEmpty(String s) {
		return s == null || s.equals("");
	}

	/**
	 * Contains common HTTP status codes used by the system.
	 *
	 */
	public final static class HttpStatusCodeRepresentation {
		public static final String OK = "200 OK";
		public static final String BAD_REQUEST = "400 Bad Request";
		public static final String UNAUTHORIZED = "401 Unauthorized";
		public static final String FORBIDDEN = "403 Forbidden";
		public static final String NOT_FOUND = "404 Not Found";
		public static final String INTERNAL_SERVER_ERROR = "500 Internal Server Error";
	}
}