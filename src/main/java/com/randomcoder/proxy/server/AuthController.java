package com.randomcoder.proxy.server;

import java.io.*;

import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Controller which implements the auth operation for a proxy connection. This
 * operation simply outputs "OK" and relies on a BASIC authentication provider
 * to authenticate first.
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
public class AuthController extends AbstractController
{
	private static final Logger logger = Logger.getLogger(AuthController.class);
	
	/**
	 * Processes the auth request.
	 * 
	 * @param request
	 *          HTTP request
	 * @param response
	 *          HTTP response
	 * @throws IOException
	 *           if an I/O error occurs
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	throws IOException
	{
		if (logger.isDebugEnabled())
			logger.debug("Auth");
		
		response.setContentType("text/plain");
		response.setContentLength(4);
		
		PrintWriter out = null;
		try
		{
			out = response.getWriter();
			out.print("OK\r\n");
		}
		finally
		{
			try { if (out != null) out.close(); } catch (Throwable ignored) {}
		}
		
		return null;
	}
}