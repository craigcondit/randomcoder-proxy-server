package org.randomcoder.proxy.handlers;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.log4j.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.randomcoder.proxy.support.*;

/**
 * Handler which establishes a new tunneled connection.
 */
public class ConnectHandler extends AbstractHandler
{
	private static final Logger logger = LogManager.getLogger(ConnectHandler.class);

	private final String path;
	private final EndpointTracker tracker;

	/**
	 * Creates a new connect handler.
	 * 
	 * @param path
	 *            base URL
	 * @param tracker
	 *            endpoint tracker
	 */
	public ConnectHandler(String path, EndpointTracker tracker)
	{
		this.path = path + "/connect";
		this.tracker = tracker;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		if (!path.equals(request.getRequestURI()))
		{
			return;
		}

		// create connection
		String host = null;
		int port = -1;
		Endpoint endpoint = null;

		try
		{
			host = request.getParameter("host");
			port = Integer.parseInt(request.getParameter("port"));
			endpoint = new SocketEndpoint(host, port);
		}
		catch (Exception e)
		{
			// can't connect
			sendMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ERROR " + e.getMessage());
		}

		// add to tracker
		String id = tracker.add(endpoint);

		logger.info("Connect [" + id + "]: user=" + CurrentUser.get() + ", destination=" + host + ":" + port);

		// write out id to response
		sendMessage(response, HttpServletResponse.SC_OK, "OPEN " + id);
	}

	private void sendMessage(HttpServletResponse response, int status, String message)
			throws IOException
	{
		response.setContentType("text/plain");
		response.setStatus(status);

		PrintWriter out = null;
		try
		{
			out = response.getWriter();
			out.print(message);
			out.print("\r\n");
		}
		finally
		{
			try
			{
				if (out != null)
					out.close();
			}
			catch (Throwable ignored)
			{
			}
		}
	}
}
