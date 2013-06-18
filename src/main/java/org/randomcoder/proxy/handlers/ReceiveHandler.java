package org.randomcoder.proxy.handlers;

import java.io.*;
import java.net.SocketException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.log4j.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.randomcoder.proxy.support.*;

/**
 * Handler which establishes and maintains a receive connection to the
 * underlying I/O stream.
 */
public class ReceiveHandler extends AbstractHandler
{
	private static final Logger logger = LogManager.getLogger(ReceiveHandler.class);

	private final String path;
	private final EndpointTracker tracker;

	/**
	 * Creates a new receive handler.
	 * 
	 * @param path
	 *            base URL
	 * @param tracker
	 *            endpoint tracker
	 */
	public ReceiveHandler(String path, EndpointTracker tracker)
	{
		this.path = path + "/receive";
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
				logger.debug("Receive [" + id + "]: user=" + CurrentUser.get() + ", state=closed");

			sendError(response, "Connection closed");
			baseRequest.setHandled(true);
			return;
		}

		if (logger.isDebugEnabled())
			logger.debug("Receive [" + id + "]: user=" + CurrentUser.get() + ", state=active");

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/octet-stream");

		DataOutputStream out = null;
		KeepaliveThread ka = null;
		try
		{
			out = new DataOutputStream(response.getOutputStream());
			out.flush();
			InputStream endpointStream = endpoint.getInputStream();

			// must send something here so that server will actually flush the
			// result

			out.write("SENDING\r\n".getBytes("UTF-8"));
			out.flush();

			ka = new KeepaliveThread(id, out);
			ka.start();

			byte[] buf = new byte[32768];
			int c;
			do
			{
				c = endpointStream.read(buf, 0, 32768);
				if (c > 0)
				{
					logger.debug("Wrote " + c + " bytes");
					ka.activity();
					sendPacket(out, buf, 0, c);

					if (!tracker.refresh(id))
						break;
				}
			}
			while (c >= 0);

			tracker.receiveComplete(id);
		}
		catch (SocketException e)
		{
			logger.debug("Receive [" + id + "]: user=" + CurrentUser.get() + ", error=" + e.getMessage());
			tracker.receiveError(id);
		}
		finally
		{
			if (ka != null)
			{
				ka.interrupt();
				try
				{
					ka.join(30000L);
				}
				catch (InterruptedException ignored)
				{
				}
				ka = null;
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

	private void sendPacket(DataOutputStream out, byte[] buf, int offset, int len)
			throws IOException
	{
		synchronized (out)
		{
			out.writeInt(len);
			if (len > 0)
			{
				out.write(buf, offset, len);
			}
			out.flush();
		}
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

	private class KeepaliveThread extends Thread
	{
		private volatile boolean shutdown = false;
		private volatile boolean idle = true;
		private final DataOutputStream dos;
		private final byte[] NULL = new byte[0];

		public KeepaliveThread(String id, DataOutputStream dos)
		{
			super("Keepalive " + id);
			this.dos = dos;
		}

		@Override
		public void run()
		{
			while (!shutdown)
			{
				try
				{
					idle = true;
					Thread.sleep(30000L);
				}
				catch (InterruptedException ignored)
				{
				}
				if (!shutdown && idle)
				{
					try
					{
						sendPacket(dos, NULL, 0, 0);
					}
					catch (IOException e)
					{
						logger.error("Error in keepalive thread", e);
					}
				}
			}
		}

		public synchronized void shutdown()
		{
			shutdown = true;
			interrupt();
		}

		public void activity()
		{
			idle = false;
		}
	}
}
