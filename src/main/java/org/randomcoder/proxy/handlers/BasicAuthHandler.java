package org.randomcoder.proxy.handlers;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.log4j.*;
import org.eclipse.jetty.http.security.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.randomcoder.proxy.support.CurrentUser;

/**
 * BASIC authentication handler. 
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
public class BasicAuthHandler extends AbstractHandler
{
	private static final Logger logger = LogManager.getLogger(BasicAuthHandler.class);
	
	private static final String REALM = "Proxy";
	
	private final Map<String, String> userMap;
	
	public BasicAuthHandler(File passwdFile) throws IOException
	{
		Map<String, String> map = new HashMap<String, String>();
		
		FileReader fr = null;
		BufferedReader br = null;
		try
		{
			fr = new FileReader(passwdFile);
			br = new BufferedReader(fr);
			String line = null;
			while ((line = br.readLine()) != null)
			{
				String[] parts = line.split(":");
				if (parts.length >= 2)
				{
					map.put(parts[0], parts[1]);
				}
			}				
		}
		finally
		{
			if (br != null) try { br.close(); } catch (Exception ignored) {}
			if (fr != null) try { fr.close(); } catch (Exception ignored) {}
		}

		logger.info("Initialized user store with " + map.size() + " entries");
		userMap = Collections.unmodifiableMap(map);
	}
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		String username = getUser(request.getHeader("Authorization"));
		if (username == null)
		{
			CurrentUser.logout();
			response.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			baseRequest.setHandled(true);
			return;
		}
		CurrentUser.login(username);
	}
	
	private String getUser(String auth)
	{
		try
		{
			if (auth == null)
			{
				return null;
			}
			
			String[] parts = auth.split(" ");
			if (parts.length < 2)
			{
				return null;
			}
			
			if (!("basic".equals(parts[0].toLowerCase(Locale.US))))
			{
				return null;
			}
			
			String token = B64Code.decode(parts[1]);
			String[] userpass = token.split(":");
			if (userpass.length != 2)
			{
				return null;
			}
			
			String hash = userMap.get(userpass[0]);
			if (hash == null)
			{
				return null;
			}
			
			if (!(hash.equals(UnixCrypt.crypt(userpass[1], hash))))
			{
				return null;
			}
			return userpass[0];
		}
		catch (Exception e)
		{
			logger.warn("Invalid auth token received", e);
			return null;
		}
	}
}
