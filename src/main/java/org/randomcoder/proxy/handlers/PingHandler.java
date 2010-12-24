package org.randomcoder.proxy.handlers;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.log4j.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.randomcoder.proxy.support.*;

/**
 * Handler which acts as a keepalive.
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

public class PingHandler extends AbstractHandler
{
	private static final Logger logger = LogManager.getLogger(PingHandler.class);
	
	private final String path;
	private final EndpointTracker tracker;
	
	public PingHandler(String path, EndpointTracker tracker)
	{
		this.path = path + "/ping";
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
		boolean active = tracker.refresh(id);

		if (logger.isDebugEnabled())
			logger.debug("Ping [" + id + "]: user=" + CurrentUser.get() + ", status=" + (active ? "active" : "closed"));
		
		response.setStatus(active ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
		response.setContentType("text/plain");
		
		PrintWriter out = null;
		try
		{
			out = response.getWriter();
			out.print(active ? "ACTIVE\r\n" : "CLOSED\r\n");
		}
		finally
		{
			try { if (out != null) out.close(); } catch (Throwable ignored) {}
		}
		baseRequest.setHandled(true);
	}
}
