package com.randomcoder.proxy.client;

import java.io.IOException;
import java.net.*;

import org.apache.log4j.Logger;

public class ProxyClient
{
	private static final Logger logger = Logger.getLogger(ProxyClient.class);
	
	private static final String REMOTE_HOST = "strategichosts.com";
	private static final int REMOTE_PORT = 22;
	private static final String PROXY_URL = "http://localhost/proxy";
	private static final int LOCAL_PORT = 12345;
	
	public void listen() throws IOException
	{
		ServerSocket ss = new ServerSocket(LOCAL_PORT);
		
		logger.debug("Listening for connections");

		while (true)
		{
			Socket socket = ss.accept();
			
			logger.debug("Connection received");
			
			SocketListenerThread listener = new SocketListenerThread(socket, PROXY_URL, REMOTE_HOST, REMOTE_PORT);
			logger.debug("Listener created");
			
			listener.start();
			logger.debug("Listener started");
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			ProxyClient client = new ProxyClient();

			client.listen();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}