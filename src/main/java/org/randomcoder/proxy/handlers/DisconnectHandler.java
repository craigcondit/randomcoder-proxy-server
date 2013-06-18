package org.randomcoder.proxy.handlers;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.log4j.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.randomcoder.proxy.support.*;

/**
 * Handler which disconnects the underlying I/O stream.
 */
public class DisconnectHandler extends AbstractHandler
{
	private static final Logger logger = LogManager.getLogger(DisconnectHandler.class);

	private final String path;
	private final EndpointTracker tracker;

	/**
	 * Creates a new disconnect handler.
	 * 
	 * @param path
	 *            base URL
	 * @param tracker
	 *            endpoint tracker
	 */
	public DisconnectHandler(String path, EndpointTracker tracker)
	{
		this.path = path + "/disconnect";
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

		String id = request.getParameter("id");
		tracker.remove(id);

		logger.info("Disconnect [" + id + "]: user=" + CurrentUser.get());

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");

		PrintWriter out = null;
		try
		{
			out = response.getWriter();
			out.print("CLOSED\r\n");
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

		baseRequest.setHandled(true);
	}

}
