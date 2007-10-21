package com.randomcoder.proxy.client;

import java.io.*;
import java.net.*;
import java.text.DecimalFormat;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

public class SocketListenerThread extends Thread
{
	private static final Logger logger = Logger.getLogger(SocketListenerThread.class);
	
	private final Socket socket;
	private final HttpClient client;
	private final MultiThreadedHttpConnectionManager connectionManager;
	private final String proxyUrl;
	private final String host;
	private final int port;
	private final Authenticator auth;
	
	public SocketListenerThread(Socket socket, String proxyUrl, String host, int port, Authenticator auth)
	{
		logger.debug("Socket listener created");
		logger.debug("  Proxy URL: " + proxyUrl);
		logger.debug("  Remote host: " + host);
		logger.debug("  Remote port: " + port);
		
		this.socket = socket;
		this.proxyUrl = proxyUrl;
		this.host = host;
		this.port = port;
		this.auth = auth;
				
		connectionManager = new MultiThreadedHttpConnectionManager();
		client = new HttpClient(connectionManager);
		client.getParams().setAuthenticationPreemptive(true);
		
	}
	
	@Override
	public void run()
	{
		logger.debug("Socket listener running");
		
		InputStream socketInput = null;
		OutputStream socketOutput = null;
		ProxyInputStream proxyInput = null;
		ProxyOutputStream proxyOutput = null;
		String connectionId = null;
		
		try
		{
			socketInput = socket.getInputStream();
			socketOutput = socket.getOutputStream();
			
			URL authUrl = new URL(proxyUrl);

			String proxyHost = authUrl.getHost();

			int proxyPort = authUrl.getPort();
			if (proxyPort < 0)
			{
				if ("http".equals(authUrl.getProtocol()))
					proxyPort = 80;
				else if ("https".equals(authUrl.getProtocol()))
					proxyPort = 443;
			}
			
			boolean force = false;
			
			do
			{
				Credentials creds = auth.getCredentials(proxyUrl, force);
				if (creds == null)
					throw new IOException("No credentials supplied");

				client.getState().setCredentials(new AuthScope(proxyHost, proxyPort, AuthScope.ANY_REALM), creds);		
				force = true;
			}
			while (!authenticate());
			
			logger.debug("Connecting to remote socket...");
			connectionId = connect();
			logger.debug("Connection complete.");			
			
			proxyOutput = new ProxyOutputStream(client, proxyUrl, connectionId);
			proxyInput = new ProxyInputStream(client, proxyUrl, connectionId);
			
			logger.debug("Proxy streams created");
					
			StreamCopyThread proxyToSocket = new StreamCopyThread(proxyInput, socketOutput);
			StreamCopyThread socketToProxy = new StreamCopyThread(socketInput, proxyOutput);
			
			proxyToSocket.start();
			socketToProxy.start();
			
			while (true)
			{				
				try { proxyToSocket.join(1000); } catch (InterruptedException ignored) {}
				if (!proxyToSocket.isAlive())
				{
					if (proxyToSocket.isSuccess())
					{
						logger.debug("Proxy to socket thread terminated. Bytes copied = " + socketToProxy.getBytesCopied());
					}
					else
					{
						IOException error = proxyToSocket.getException();
						if (error == null)
						{
							logger.error("Proxy to socket thread terminated with unknown error");
						}
						else
						{
							logger.error("Proxy to socket thread terminated with error", error);
						}
					}
					break;
				}
				
				try { socketToProxy.join(1000); } catch (InterruptedException ignored) {}
				if (!socketToProxy.isAlive())
				{
					if (socketToProxy.isSuccess())
					{
						logger.debug("Socket to proxy thread terminated. Bytes copied = " + socketToProxy.getBytesCopied());
					}
					else
					{
						IOException error = socketToProxy.getException();
						if (error == null)
						{
							logger.error("Socket to proxy thread terminated with unknown error");
						}
						else
						{
							logger.error("Socket to proxy thread terminated with error", error);
						}
					}
					break;
				}				
			}
		}
		catch (Throwable t)
		{
			// log uncaught exceptions
			logger.error("Error during socket listener setup", t);
		}
		finally
		{
			logger.debug("Shutting down streams...");
			
			logger.debug("  Socket input...");
			try { if (socketInput != null) socketInput.close(); } catch (Throwable ignored) {}
			
			logger.debug("  Socket output...");
			
			try { if (socketOutput != null) socketOutput.close(); } catch (Throwable ignored) {}
			
			logger.debug("  Proxy input...");
			try { if (proxyInput != null) proxyInput.close(); } catch (Throwable ignored) {}
			
			logger.debug("  Proxy output...");
			try { if (proxyOutput != null) proxyOutput.close(); } catch (Throwable ignored) {}
	
			logger.debug("  Socket...");
			try { if (socket != null) socket.close(); } catch (Throwable ignored) {}
			
			logger.debug("  Calling disconnect...");
			try {	if (connectionId != null)	disconnect(connectionId); } catch (Throwable ignored) {}
			
			logger.debug("Closing all connections...");
			connectionManager.deleteClosedConnections();
			
			logger.debug("Socket listener terminated");
		}
	}
	
	private boolean authenticate() throws IOException
	{
		GetMethod get = null;
		try
		{
			get = new GetMethod(proxyUrl + "/auth");		
			get.setDoAuthentication(true);
			get.setFollowRedirects(false);
			get.getParams().setVersion(HttpVersion.HTTP_1_1);
			get.setRequestHeader("User-Agent", "Randomcoder-Proxy 1.0-SNAPSHOT");
			
			int status = client.executeMethod(get);
			if (status == HttpStatus.SC_OK)
			{
				get.getResponseBodyAsString();
				return true;
			}
			
			if (status == HttpStatus.SC_UNAUTHORIZED)
			{
				// bad
				get.getResponseBodyAsString();
				return false;
			}
			
			// ugly
			throw new IOException("Unknown status received from remote proxy: " + status);
		}
		finally
		{
			try { if (get != null) get.releaseConnection(); } catch (Throwable ignored) {}
		}
	}
	
	private String connect() throws IOException
	{
		GetMethod get = null;
		try
		{
			DecimalFormat df = new DecimalFormat("##########");
			get = new GetMethod(proxyUrl + "/connect?host=" + URLEncoder.encode(host, "UTF-8") + "&port=" + df.format(port));		
			get.setDoAuthentication(true);
			get.setFollowRedirects(false);
			get.getParams().setVersion(HttpVersion.HTTP_1_1);
			get.getParams().setSoTimeout(0);
			get.setRequestHeader("User-Agent", "Randomcoder-Proxy 1.0-SNAPSHOT");
			
			int status = client.executeMethod(get);
			if (status == HttpStatus.SC_OK)
			{
				// good
				String response = get.getResponseBodyAsString().trim();
				String[] parts = response.split(" ", 3);
				if (parts.length != 2 || !("OPEN".equals(parts[0])))
					throw new IOException("Garbled response from remote proxy: " + response);
			
				return parts[1]; // connection id
			}
			
			if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
			{
				// bad
				String response = get.getResponseBodyAsString();
				throw new IOException(response);
			}
			
			// ugly
			throw new IOException("Unknown status received from remote proxy: " + status);
		}
		finally
		{
			try { if (get != null) get.releaseConnection(); } catch (Throwable ignored) {}
		}
	}
	
	private void disconnect(String connectionId) throws IOException
	{
		GetMethod get = null;
		try
		{
			get = new GetMethod(proxyUrl + "/disconnect?id=" + URLEncoder.encode(connectionId, "UTF-8"));		
			get.setDoAuthentication(true);
			get.setFollowRedirects(false);
			get.getParams().setVersion(HttpVersion.HTTP_1_1);
			get.getParams().setSoTimeout(0);
			get.setRequestHeader("User-Agent", "Randomcoder-Proxy 1.0-SNAPSHOT");
			
			int status = client.executeMethod(get);
			if (status == HttpStatus.SC_OK)
			{
				// good
				String response = get.getResponseBodyAsString().trim();
				if (!("CLOSED".equals(response)))
					throw new IOException("Garbled response from remote proxy: " + response);
			}
			
			// ugly
			throw new IOException("Unknown status received from remote proxy: " + status);
		}
		finally
		{
			try { if (get != null) get.releaseConnection(); } catch (Throwable ignored) {}
		}		
	}
}