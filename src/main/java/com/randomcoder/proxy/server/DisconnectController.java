package com.randomcoder.proxy.server;

import java.io.*;

import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * Controller which closes an active connection.
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
public class DisconnectController extends AbstractCommandController
{
	private static final Logger logger = Logger.getLogger(DisconnectController.class);
	
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
	 * Processes the disconnect request.
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
		
		endpointTracker.remove(form.getId());
		
		if (logger.isDebugEnabled())
			logger.debug("Disconnect [" + form.getId() + "]");
		
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
			try { if (out != null) out.close(); } catch (Throwable ignored) {}
		}
		
		return null;
	}
}