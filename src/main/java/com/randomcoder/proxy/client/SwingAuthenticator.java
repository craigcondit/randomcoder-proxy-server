package com.randomcoder.proxy.client;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.httpclient.*;

public class SwingAuthenticator implements Authenticator
{
	private final ConcurrentHashMap<String, Credentials> credMap = new ConcurrentHashMap<String, Credentials>();
	
	public Credentials getCredentials(String proxyUrl, boolean force)
	{
		Credentials creds = credMap.get(proxyUrl);
		
		if (creds == null || force)
		{
			PasswordDialog prompt = new PasswordDialog(null, proxyUrl);
	
			prompt.setVisible(true);
			
			String username = prompt.getUsername();
			String password = prompt.getPassword();
			
			if (username == null || password == null)
				return null;

			creds = new UsernamePasswordCredentials(username, password);
			
			credMap.put(proxyUrl, creds);
		}
		
		return creds;
	}
}