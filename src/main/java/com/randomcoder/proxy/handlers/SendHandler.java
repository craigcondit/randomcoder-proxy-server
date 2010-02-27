package com.randomcoder.proxy.handlers;

import java.io.*;
import java.text.DecimalFormat;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.randomcoder.proxy.support.*;

/**
 * Handler which accepts messages and sends them to the underlying I/O stream.
 * 
 * <pre>
 * Copyright (c) 2010, Craig Condit. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
public class SendHandler extends AbstractHandler
{
	private static final Logger logger = LogManager.getLogger(SendHandler.class);
	
	private final String path;
	private final EndpointTracker tracker;
	
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
			try { if (in != null) in.close(); } catch (Throwable ignored) {}
			try { if (out != null) out.close(); } catch (Throwable ignored) {}
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
			try { if (out != null) out.close(); } catch (Throwable ignored) {}
		}
	}
	
}