package com.randomcoder.proxy.client;

import java.io.IOException;
import java.net.*;

import org.apache.log4j.Logger;

public class ProxyClient
{
	private static final Logger logger = Logger.getLogger(ProxyClient.class);
	
	private final String proxyUrl;
	private final String remoteHost;
	private final int remotePort;
	private final int localPort;

	public ProxyClient(String proxyUrl, String remoteHost, int remotePort, int localPort)
	{
		this.proxyUrl = proxyUrl;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.localPort = localPort;
	}
	
	public void listen() throws IOException
	{
		ServerSocket ss = new ServerSocket(localPort);
		
		logger.debug("Listening for connections");

		while (true)
		{
			Socket socket = ss.accept();
			
			logger.debug("Connection received");
			
			SocketListenerThread listener = new SocketListenerThread(socket, proxyUrl, remoteHost, remotePort);
			logger.debug("Listener created");
			
			listener.start();
			logger.debug("Listener started");
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			if (args.length != 4)
			{
				usage();
				return;
			}

			String proxyUrl = args[0];
			proxyUrl = proxyUrl.replaceAll("/*$", "");
			System.err.println("Proxy URL: " + proxyUrl);
			
			String remoteHost = args[1];
			int remotePort = Integer.parseInt(args[2]);
			int localPort = Integer.parseInt(args[3]);
			
			ProxyClient client = new ProxyClient(proxyUrl, remoteHost, remotePort, localPort);	
			client.listen();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	private static void usage()
	{
		System.err.print("Usage: ");
		System.err.print(ProxyClient.class.getName());
		System.err.println(" <proxy-url> <remote-host> <remote-ip> <local-ip>");
		System.err.println();
		System.err.println("  proxy-url     Fully qualified URL to the remote HTTP proxy");
		System.err.println("  remote-host   Remote hostname to connect to");
		System.err.println("  remote-ip     Remote IP address to connect to");
		System.err.println("  local-ip      Local IP address to listen on");
		System.err.println();
	}
}