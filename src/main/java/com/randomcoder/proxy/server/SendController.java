package com.randomcoder.proxy.server;

import java.io.*;
import java.text.DecimalFormat;

import javax.servlet.ServletInputStream;
import javax.servlet.http.*;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * Controller which handles the send event on a connection. This event posts
 * data destined for the output stream of the underlying connection.
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
public class SendController extends AbstractCommandController
{
	private static final Logger logger = Logger.getLogger(SendController.class);
	
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
	 * Processes the send request.
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

		endpointTracker.refresh(form.getId());
		
		Endpoint endpoint = endpointTracker.getEndpoint(form.getId());
		
		if (endpoint == null)
		{
			if (logger.isDebugEnabled())
				logger.debug("Send [" + form.getId() + "]: closed");

			sendError(response, "Connection closed");
			return null;
		}

		ServletInputStream in = null;
		PrintWriter out = null;		
		try
		{
			in = request.getInputStream();
			int bytes = IOUtils.copy(in, endpoint.getOutputStream());
			
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");
			
			DecimalFormat df = new DecimalFormat("##########");
			out = response.getWriter();
			out.print("RECEIVED " + df.format(bytes));
			
			if (logger.isDebugEnabled())
				logger.debug("Receive [" + form.getId() + "]: received " + bytes + " bytes");
		}
		finally
		{
			try { if (in != null) in.close(); } catch (Throwable ignored) {}
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