package com.randomcoder.proxy.client;

import java.io.IOException;
import java.net.*;

import org.apache.log4j.Logger;

import com.randomcoder.proxy.client.gui.SwingAuthenticator;

/**
 * HTTP proxy client.
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
public class ProxyClient
{
	private static final Logger logger = Logger.getLogger(ProxyClient.class);
	
	private final String proxyUrl;
	private final String remoteHost;
	private final int remotePort;
	private final int localPort;
	private final Authenticator auth;
	
	/**
	 * Creates a new proxy client.
	 * 
	 * @param proxyUrl
	 *            base URL of remote proxy
	 * @param remoteHost
	 *            host to connect to on remote side
	 * @param remotePort
	 *            port to connect to on remote side
	 * @param localPort
	 *            local port to listen on
	 */
	public ProxyClient(String proxyUrl, String remoteHost, int remotePort, int localPort)
	{
		this.proxyUrl = proxyUrl;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.localPort = localPort;
		auth = new SwingAuthenticator();
	}
	
	/**
	 * Listens for incoming connections.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void listen() throws IOException
	{
		ServerSocket ss = new ServerSocket();
		ss.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), localPort));
		
		logger.debug("Listening for connections");

		Socket socket = null;
		
		while (true)
		{
			try
			{
				socket = ss.accept();

				logger.debug("Connection received");
				
				SocketListenerThread listener = new SocketListenerThread(socket, proxyUrl, remoteHost, remotePort, auth);
				logger.debug("Listener created");
				
				listener.start();
				logger.debug("Listener started");
				socket = null;
			}
			catch (Throwable t)
			{
				try { if (socket != null) socket.close(); } catch (Throwable ignored) {}
				logger.error("Caught exception", t);
			}
		}
	}
	
	/**
	 * Main entry point for application.
	 * 
	 * @param args
	 *            unused
	 */
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
			
			// TODO add system tray icon and menu if supported			
			// if not, display main configure screen
			// also, if no tunnels could be created, show configure screen
			
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			
//			if (SystemTrayWrapper.isSupported())
//			{
//				// system tray available
//				SystemTrayWrapper tray = SystemTrayWrapper.getSystemTray();
//				
//				BufferedImage image = ImageIO.read(ProxyClient.class.getResourceAsStream("/socket.png"));
//				
//				PopupMenu popup = new PopupMenu();
//				
//				popup.setFont(new JMenuItem("test").getFont());
//				
//				MenuItem configure = new MenuItem("Configure");
//				configure.addActionListener(new ActionListener()
//				{
//					public void actionPerformed(ActionEvent e)
//					{
//						// TODO open configuration screen
//						System.err.println("Configure chosen");
//					}
//				});				
//				popup.add(configure);
//				configure.setFont(popup.getFont().deriveFont(Font.BOLD));
//				
//				popup.addSeparator();
//				
//				MenuItem exit = new MenuItem("Exit");
//				exit.addActionListener(new ActionListener()
//				{
//					public void actionPerformed(ActionEvent e)
//					{
//						System.exit(0);
//					}
//				});
//				popup.add(exit);
//				TrayIconWrapper icon = new TrayIconWrapper(image, "Disconnected", popup);
//				icon.addActionListener(new ActionListener()
//				{
//					public void actionPerformed(ActionEvent e)
//					{
//						// TODO open configuration screen
//						System.err.println("Default action chosen - configure");
//					}
//				});
//				icon.setImageAutoSize(true);
//				tray.add(icon);
//				
//			}
//			else
//			{
//				System.err.println("No system tray found");
//			}
			
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