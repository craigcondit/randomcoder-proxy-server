package org.randomcoder.proxy;

import java.io.*;
import java.security.SecureRandom;
import java.util.*;

import org.eclipse.jetty.http.security.UnixCrypt;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.randomcoder.proxy.handlers.*;
import org.randomcoder.proxy.support.EndpointTracker;

/**
 * HTTP tunneling proxy server.
 */
public class ProxyServer
{
	private static final int DEFAULT_PORT = 9999;
	private static final String DEFAULT_PATH = "/proxy";
	private static final String DEFAULT_HOST = "127.0.0.1";
	private static final String DEFAULT_AUTH = "/srv/randomcoder-proxy/etc/passwd";

	private static final String HOST_PARAM = "proxy.host";
	private static final String PORT_PARAM = "proxy.port";
	private static final String PATH_PARAM = "proxy.path";
	private static final String AUTH_PARAM = "auth.file";

	private final EndpointTracker tracker;
	private final Server server;

	/**
	 * Creates a new proxy server.
	 * 
	 * @param host
	 *            host to listen on
	 * @param port
	 *            port number to listen on
	 * @param context
	 *            base URL
	 * @param passwdFile
	 *            password file
	 * @throws IOException
	 *             if an error occurs
	 */
	public ProxyServer(String host, int port, String context, File passwdFile)
			throws IOException
	{
		tracker = new EndpointTracker();
		server = new Server();

		if (context == "/")
		{
			context = "";
		}

		SelectChannelConnector con = new SelectChannelConnector();
		con.setPort(port);
		con.setHost(host);

		server.setConnectors(new Connector[] { con });

		HandlerList handlers = new HandlerList();

		// authentication handler must be first
		handlers.addHandler(new BasicAuthHandler(passwdFile));

		// remaining handlers are in order of decreasing frequency of calls
		handlers.addHandler(new SendHandler(context, tracker));
		handlers.addHandler(new PingHandler(context, tracker));
		handlers.addHandler(new ReceiveHandler(context, tracker));
		handlers.addHandler(new ConnectHandler(context, tracker));
		handlers.addHandler(new DisconnectHandler(context, tracker));
		handlers.addHandler(new AuthHandler(context));
		handlers.addHandler(new StatusHandler(context, tracker));

		server.setHandler(handlers);
	}

	/**
	 * Starts the server.
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	public void start() throws Exception
	{
		server.start();
	}

	/**
	 * Stops the server.
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	public void stop() throws Exception
	{
		server.stop();
		tracker.destroy();
	}

	/**
	 * Entry point.
	 * 
	 * @param args
	 *            arguments
	 * @throws Exception
	 *             if an error occurs
	 */
	public static void main(String[] args) throws Exception
	{
		LinkedList<String> params = new LinkedList<String>(Arrays.asList(args));
		if (params.size() > 0)
		{
			String command = params.remove();
			if ("encrypt".equals(command) && params.size() == 1)
			{
				String username = params.remove();
				Console console = System.console();
				if (console == null)
				{
					System.err.println("Error: Console not available");
					return;
				}

				System.err.print("Enter password: ");
				System.err.flush();
				char[] password = console.readPassword();
				Random random = new SecureRandom();
				String digits = new String("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwyxz");
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < 16; i++)
				{
					buf.append(digits.charAt(random.nextInt(digits.length())));
				}
				System.out.println(username + ":" + UnixCrypt.crypt(new String(password), buf.toString()));
				return;
			}
			usage();
			return;
		}

		String host = System.getProperty(HOST_PARAM, DEFAULT_HOST);
		int port = Integer.parseInt(System.getProperty(PORT_PARAM, Integer.toString(DEFAULT_PORT)));
		String context = System.getProperty(PATH_PARAM, DEFAULT_PATH);
		String passwd = System.getProperty(AUTH_PARAM, DEFAULT_AUTH);

		File passwdFile = new File(passwd);
		if (!passwdFile.exists())
		{
			System.err.println("Error: Password file " + passwdFile.getAbsolutePath() + " does not exist.");
			return;
		}

		final ProxyServer proxy = new ProxyServer(host, port, context, passwdFile);
		proxy.start();

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					proxy.stop();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.exit(1);
				}
			}
		});
	}

	/**
	 * Displays server usage.
	 */
	public static void usage()
	{
		System.err.println("Usage: " + ProxyServer.class.getName() + " [command]");
		System.err.println("  where command is one of the following:");
		System.err.println("    encrypt <username> -- prompts for a password and writes a password entry to standard out.");
		System.err.println("    usage -- display this message");
		System.err.println();
		System.err.println("  If no command is entered, the proxy server will start up.");
		System.err.println();
		System.err.println("  Supported java properties:");
		System.err.println("    " + HOST_PARAM + " -- hostname to listen on [" + DEFAULT_HOST + "]");
		System.err.println("    " + PORT_PARAM + " -- port to listen on [" + DEFAULT_PORT + "]");
		System.err.println("    " + PATH_PARAM + " -- context path [" + DEFAULT_PATH + "]");
		System.err.println("    " + AUTH_PARAM + " -- password file [" + DEFAULT_AUTH + "]");
	}
}