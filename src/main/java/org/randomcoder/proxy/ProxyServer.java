package org.randomcoder.proxy;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.rewrite.handler.HeaderPatternRule;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RuleContainer;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.security.UnixCrypt;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.randomcoder.proxy.handlers.AuthHandler;
import org.randomcoder.proxy.handlers.BasicAuthHandler;
import org.randomcoder.proxy.handlers.ConnectHandler;
import org.randomcoder.proxy.handlers.DisconnectHandler;
import org.randomcoder.proxy.handlers.PingHandler;
import org.randomcoder.proxy.handlers.ReceiveHandler;
import org.randomcoder.proxy.handlers.SendHandler;
import org.randomcoder.proxy.handlers.StatusHandler;
import org.randomcoder.proxy.support.EndpointTracker;

/**
 * HTTP tunneling proxy server.
 */
public class ProxyServer {
	private static final int DEFAULT_PORT = 9999;
	private static final String DEFAULT_PATH = "/proxy";
	private static final String DEFAULT_HOST = "127.0.0.1";
	private static final String DEFAULT_AUTH = "/srv/randomcoder-proxy/etc/passwd";
	private static final String DEFAULT_HTTP_FORWARDED = "false";
	private static final String DEFAULT_HTTPS_FORCED = "false";
	private static final String DEFAULT_STS_MAX_AGE = "0";

	private static final String HOST_PARAM = "proxy.host";
	private static final String PORT_PARAM = "proxy.port";
	private static final String PATH_PARAM = "proxy.path";
	private static final String AUTH_PARAM = "auth.file";
	private static final String HTTP_FORWARDED_PARAM = "http.forwarded";
	private static final String HTTPS_FORCED_PARAM = "https.forced";
	private static final String STS_MAX_AGE_PARAM = "sts.max.age";

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
	 * @param forward
	 *            whether to do X-Forwarded-{For,Proto} handling
	 * @param forceHttps
	 *            whether to force a redirect to HTTPS
	 * @param sendSts
	 *            whether to send Strict-Transport-Security header
	 * @param stsMaxAge
	 *            maximum age of STS header
	 * @throws IOException
	 *             if an error occurs
	 */
	public ProxyServer(String host, int port, String context, File passwdFile, boolean forward, boolean forceHttps,
			boolean sendSts, long stsMaxAge) throws IOException {
		tracker = new EndpointTracker();

		if (context == "/") {
			context = "";
		}

		QueuedThreadPool threadPool = new QueuedThreadPool(100, 10, 60000);

		server = new Server(threadPool);
		server.addBean(new ScheduledExecutorScheduler());

		HttpConfiguration httpConfig = new HttpConfiguration();
		httpConfig.setSecurePort(443);

		if (forward) {
			httpConfig.addCustomizer(new ForwardedRequestCustomizer());
		}

		HandlerCollection handlers = new HandlerCollection();

		if (forceHttps) {
			handlers.addHandler(new SecuredRedirectHandler());
		}

		if (sendSts) {
			RewriteHandler rewriteHandler = new RewriteHandler();
			RuleContainer rules = new RuleContainer();

			HeaderPatternRule stsRule = new HeaderPatternRule();
			stsRule.setPattern("/*");
			stsRule.setName("Strict-Transport-Security");
			stsRule.setValue("max-age=" + new DecimalFormat("####################").format(stsMaxAge));
			rules.addRule(stsRule);
			rewriteHandler.setRuleContainer(rules);
			handlers.addHandler(rewriteHandler);
		}

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

		List<ConnectionFactory> connectionFactories = new ArrayList<>();

		// http/1.1 connector
		connectionFactories.add(new HttpConnectionFactory(httpConfig));

		// h2c connector
		HTTP2CServerConnectionFactory http2cFactory = new HTTP2CServerConnectionFactory(httpConfig);
		http2cFactory.setMaxConcurrentStreams(-1);
		http2cFactory.setInitialStreamRecvWindow(65535);
		connectionFactories.add(http2cFactory);

		ServerConnector httpConnector = new ServerConnector(server, 1, -1,
				connectionFactories.toArray(new ConnectionFactory[] {}));
		httpConnector.setHost(host);
		httpConnector.setPort(port);

		server.addConnector(httpConnector);
	}

	/**
	 * Starts the server.
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	public void start() throws Exception {
		server.start();
	}

	/**
	 * Stops the server.
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	public void stop() throws Exception {
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
	public static void main(String[] args) throws Exception {
		LinkedList<String> params = new LinkedList<String>(Arrays.asList(args));
		if (params.size() > 0) {
			String command = params.remove();
			if ("encrypt".equals(command) && params.size() == 1) {
				String username = params.remove();
				Console console = System.console();
				if (console == null) {
					System.err.println("Error: Console not available");
					return;
				}

				System.err.print("Enter password: ");
				System.err.flush();
				char[] password = console.readPassword();
				Random random = new SecureRandom();
				String digits = new String("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwyxz");
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < 16; i++) {
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
		boolean forwarded = Boolean.parseBoolean(System.getProperty(HTTP_FORWARDED_PARAM, DEFAULT_HTTP_FORWARDED));
		boolean forceHttps = Boolean.parseBoolean(System.getProperty(HTTPS_FORCED_PARAM, DEFAULT_HTTPS_FORCED));
		long stsMaxAge = Long.parseLong(System.getProperty(STS_MAX_AGE_PARAM, DEFAULT_STS_MAX_AGE), 10);
		boolean sendSts = (stsMaxAge > 0L);

		File passwdFile = new File(passwd);
		if (!passwdFile.exists()) {
			System.err.println("Error: Password file " + passwdFile.getAbsolutePath() + " does not exist.");
			return;
		}

		final ProxyServer proxy = new ProxyServer(host, port, context, passwdFile, forwarded, forceHttps, sendSts,
				stsMaxAge);
		proxy.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					proxy.stop();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		});
	}

	/**
	 * Displays server usage.
	 */
	public static void usage() {
		System.err.println("Usage: " + ProxyServer.class.getName() + " [command]");
		System.err.println("  where command is one of the following:");
		System.err.println(
				"    encrypt <username> -- prompts for a password and writes a password entry to standard out.");
		System.err.println("    usage -- display this message");
		System.err.println();
		System.err.println("  If no command is entered, the proxy server will start up.");
		System.err.println();
		System.err.println("  Supported java properties:");
		System.err.println("    " + HOST_PARAM + " -- hostname to listen on [" + DEFAULT_HOST + "]");
		System.err.println("    " + PORT_PARAM + " -- port to listen on [" + DEFAULT_PORT + "]");
		System.err.println("    " + PATH_PARAM + " -- context path [" + DEFAULT_PATH + "]");
		System.err.println("    " + AUTH_PARAM + " -- password file [" + DEFAULT_AUTH + "]");
		System.err.println("    " + HTTP_FORWARDED_PARAM + " -- whether to honor X-Forwarded-{For,Proto} headers ["
				+ DEFAULT_HTTP_FORWARDED + "]");
		System.err.println("    " + HTTPS_FORCED_PARAM + " -- force HTTPS redirects [" + DEFAULT_HTTPS_FORCED + "]");
		System.err.println(
				"    " + STS_MAX_AGE_PARAM + " -- max age for Strict-Transport-Security header [0 (disabled)]");
	}
}
