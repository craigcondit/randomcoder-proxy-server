package org.randomcoder.proxy.handlers;

import java.io.*;
import java.text.DecimalFormat;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.randomcoder.proxy.support.*;

/**
 * Handler which accepts messages and sends them to the underlying I/O stream.
 */
public class SendHandler extends AbstractHandler
{
	private static final Logger logger = LogManager.getLogger(SendHandler.class);

	private final String path;
	private final EndpointTracker tracker;

	/**
	 * Creates a new send handler.
	 * 
	 * @param path
	 *            base URL
	 * @param tracker
	 *            endpoint tracker
	 */
	public SendHandler(String path, EndpointTracker tracker)
	{
		this.path = path + "/send";
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
		Endpoint endpoint = tracker.getEndpoint(id);

		if (endpoint == null)
		{
			if (logger.isDebugEnabled())
				logger.debug("Send [" + id + "]: user=" + CurrentUser.get() + ", state=closed");

			sendError(response, "Connection closed");
			baseRequest.setHandled(true);
			return;
		}

		ServletInputStream in = null;
		ServletOutputStream out = null;
		try
		{
			in = request.getInputStream();

			OutputStream endpointOutputStream = endpoint.getOutputStream();
			byte[] buf = new byte[32768];
			int bytes = 0;
			int c = 0;
			do
			{
				c = in.read(buf, 0, 32768);
				if (c > 0)
				{
					endpointOutputStream.write(buf, 0, c);
					bytes += c;
					if (!tracker.refresh(id))
						break;
				}
			}
			while (c >= 0);

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");

			DecimalFormat df = new DecimalFormat("##########");
			byte[] message = ("RECEIVED " + df.format(bytes) + "\r\n").getBytes("UTF-8");
			response.setContentLength(message.length);

			out = response.getOutputStream();
			out.write(message);
			out.flush();

			if (logger.isDebugEnabled())
				logger.debug("Send [" + id + "]: user=" + CurrentUser.get() + ", received " + bytes + " bytes");
		}
		finally
		{
			try
			{
				if (in != null)
					in.close();
			}
			catch (Throwable ignored)
			{
			}
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

	private void sendError(HttpServletResponse response, String error)
			throws IOException
	{
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		response.setContentType("text/plain");

		PrintWriter out = null;
		try
		{
			out = response.getWriter();
			out.print("ERROR " + error + "\r\n");
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
