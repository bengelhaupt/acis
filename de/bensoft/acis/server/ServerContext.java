/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.server;

import java.util.Map;

import de.bensoft.acis.core.ACIS;

/**
 * Represents a context for the {@link Server}.
 *
 */
public class ServerContext {

	private final String mRoute;
	private final ACIS mACIS;
	private final ServerContextHandler mContextHandler;
	private final boolean mRequiresAuthentication;

	/**
	 * Constructor for a ServerContext.
	 * 
	 * @param route
	 *            The route under which the context is accessible.
	 * @param system
	 *            The {@link de.bensoft.acis.core.ACIS} system to use.
	 * @param handler
	 *            The ServerContextHandler interface.
	 * @param auth
	 *            Whether an authentication is required to access this context.
	 */
	public ServerContext(String route, ACIS system, ServerContextHandler handler, boolean auth) {
		mRoute = route;
		mACIS = system;
		mContextHandler = handler;
		mRequiresAuthentication = auth;
	}

	/**
	 * Returns the route under which the context is accessible.
	 * 
	 * @return The route.
	 */
	public String getRoute() {
		return mRoute;
	}

	/**
	 * Returns the used {@link de.bensoft.acis.core.ACIS} system.
	 * 
	 * @return The {@link de.bensoft.acis.core.ACIS} system.
	 */
	public ACIS getSystem() {
		return mACIS;
	}

	/**
	 * Returns the {@link ServerContext.ServerContextHandler} this context uses.
	 * 
	 * @return The {@link ServerContext.ServerContextHandler}.
	 */
	public ServerContextHandler getContextHandler() {
		return mContextHandler;
	}

	/**
	 * Returns whether authentication is required to access this context.
	 * 
	 * @return {@code true} when authentication is required, else {@code false}.
	 */
	public boolean requiresAuthentication() {
		return mRequiresAuthentication;
	}

	/**
	 * Represents a handler for a {@link ServerContext}.
	 *
	 */
	public static abstract interface ServerContextHandler {

		/**
		 * The method to process data.
		 * 
		 * @param system
		 *            The {@link de.bensoft.acis.core.ACIS} system to use.
		 * @param arguments
		 *            The query arguments.
		 * @return A {@link ServerContext.ServerContextResult} containing the
		 *         execution result.
		 * @throws Exception
		 *             When there is some kind of error during handling.
		 */
		public ServerContextResult handle(ACIS system, Map<String, String> arguments) throws Exception;
	}

	/**
	 * Represents a result of a handle event in the {@link Server}.<br>
	 * It consists of a response String, a status code and a content type.<br>
	 * {@link #getContentType()} returns \"text/plain; charset=utf-8\" by
	 * default.
	 *
	 */
	public static class ServerContextResult {
		private String mResponse;
		private String mStatusCodeRepresentation;
		private String mContentType = "text/plain; charset=utf-8";

		/**
		 * Constructor using a HTTP status code and a response text.
		 * 
		 * @param response
		 *            The response text.
		 * @param statusCode
		 *            The response HTTP status code.
		 */
		public ServerContextResult(String response, String statusCode) {
			mResponse = response;
			mStatusCodeRepresentation = statusCode;
		}

		/**
		 * Returns the response text.
		 * 
		 * @return The response text.
		 */
		public String getResponse() {
			return mResponse;
		}

		/**
		 * Returns the result status code.
		 * 
		 * @return The response HTTP status code.
		 */
		public String getStatusCode() {
			return mStatusCodeRepresentation;
		}

		/**
		 * Sets the HTTP-header 'Content-Type' property.<br>
		 * Note: The prefix 'Content-Type:' is not needed.
		 * 
		 * @param contentType
		 *            The content type String to set.
		 */
		public void setContentType(String contentType) {
			mContentType = contentType;
		}

		/**
		 * Returns HTTP-header content type.
		 * 
		 * @return The content type without the 'Content-Type:' prefix.
		 */
		public String getContentType() {
			return mContentType;
		}
	}
}