package com.randomcoder.proxy.server;

import java.io.*;
import java.net.SocketException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * Controller which handles the receive event on a connection. This event
 * flushes data periodically from the connected socket.
 * 
 * <pre>
 * Copyright (c) 2007, Craig Condit. All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &quot;AS IS&quot;
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
public class ReceiveController extends AbstractCommandController
{
	private static final Logger logger = Logger.getLogger(ReceiveController.class);
	
	private EndpointTracker endpointTracker;
	
	/**
	 * Sets the endpoint tracker to use.
	 * 
	 * @param endpointTracker
	 *          endpoint tracker
	 */
	public void setEndpointTracker(EndpointTracker endpointTracker)
	{
		this.endpointTracker = endpointTracker;
	}
	
	/**
	 * Processes the receive request.
	 * 
	 * @param request
	 *          HTTP request
	 * @param response
	 *          HTTP response
	 * @param command
	 *          {@link IdCommand} instance
	 * @param errors
	 *          unused
	 * @throws IOException
	 *           if an I/O error occurs
	 */
	@Override
	protected ModelAndView handle(
			HttpServletRequest request, HttpServletResponse response,
			Object command, BindException errors)
	throws IOException
	{
		IdCommand form = (IdCommand) command;

		Endpoint endpoint = endpointTracker.getEndpoint(form.getId());
		
		if (endpoint == null)
		{
			if (logger.isDebugEnabled())
				logger.debug("Receive [" + form.getId() + "]: closed");

			sendError(response, "Connection closed");
			return null;
		}

		if (logger.isDebugEnabled())
			logger.debug("Receive [" + form.getId() + "]: active");
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/octet-stream");
		
		ServletOutputStream out = null;
		try
		{
			out = response.getOutputStream();
			out.flush();
			InputStream endpointStream = endpoint.getInputStream();

			// must send something here so that server will actually flush the result
			out.write("SENDING\r\n".getBytes("UTF-8"));
			out.flush();
			
			byte[] buf = new byte[32768];
			int c;
			do
			{
				c = endpointStream.read(buf, 0, 32768);
				if (c > 0)
				{
					logger.debug("Wrote " + c + " bytes");
					out.write(buf, 0, c);
					out.flush();
					
					if (!endpointTracker.refresh(form.getId()))
						break;
				}
			}
			while (c >= 0);
		}
		catch (SocketException e)
		{
			logger.debug("Receive [" + form.getId() + "]: " + e.getMessage());
		}
		finally
		{
			try { if (out != null) out.close(); } catch (Throwable ignored) {}
		}
		
		return null;
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