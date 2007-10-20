package com.randomcoder.proxy.client;

import java.io.*;
import java.net.URLEncoder;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

public class ProxyInputStream extends InputStream
{
	private static final Logger logger = Logger.getLogger(ProxyInputStream.class);
	
	private final HttpClient client;
	private final String proxyUrl;
	private final String connectionId;
	private final GetMethod connection;
	private final InputStream inputStream;
	
	/**
	 * Creates a new input stream connected to a remote HTTP proxy.
	 * 
	 * @param client
	 *          HTTP client to use for connections
	 * @param connectionId
	 *          connection id
	 * @throws IOException
	 *           if an error occurs while establishing communications
	 */
	public ProxyInputStream(HttpClient client, String proxyUrl, String connectionId)
	throws IOException
	{
		logger.debug("Creating proxy input stream");
		
		this.client = client;
		this.proxyUrl = proxyUrl;
		this.connectionId = connectionId;
		
		connection = openConnection();
		
		logger.debug("Getting response as stream");
		
		inputStream = connection.getResponseBodyAsStream();
		
		logger.debug("Got response stream");
		
		logger.debug("Removing first line");
		
		while (inputStream.read() != '\n') {}
		
		logger.debug("Proxy input stream initialized");
	}
	
	@Override
	public int read() throws IOException
	{
		return inputStream.read();
	}

	@Override
	public int available() throws IOException
	{
		return inputStream.available();
	}

	@Override
	public void close() throws IOException
	{
		logger.debug("close()");
		
		logger.debug("Aborting connection...");
		try { connection.abort(); } catch (Throwable ignored) {}
		
		logger.debug("Closing input stream...");
		try { inputStream.close(); } catch (Throwable ignored) {}
		
		logger.debug("Releasing connection...");
		try { connection.releaseConnection(); } catch (Throwable ignored) {}
		
		logger.debug("close() complete");
	}

	@Override
	public synchronized void mark(int readlimit)
	{
		inputStream.mark(readlimit);
	}

	@Override
	public boolean markSupported()
	{
		return inputStream.markSupported();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		return inputStream.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		return inputStream.read(b);
	}

	@Override
	public synchronized void reset() throws IOException
	{
		inputStream.reset();
	}

	@Override
	public long skip(long n) throws IOException
	{
		return inputStream.skip(n);
	}

	private GetMethod openConnection()
	throws IOException
	{
		logger.debug("Opening connection");
		
		GetMethod get = new GetMethod(proxyUrl + "/receive?id=" + URLEncoder.encode(connectionId, "UTF-8"));
		
		get.setDoAuthentication(true);
		get.setFollowRedirects(false);
		get.getParams().setVersion(HttpVersion.HTTP_1_1);
		get.getParams().setSoTimeout(0);
		get.setRequestHeader("User-Agent", "Randomcoder-Proxy 1.0-SNAPSHOT");
		
		logger.debug("Executing method");
		int status = client.executeMethod(get);
		
		logger.debug("Recieve executed");
		
		if (status == HttpStatus.SC_OK)
			return get;
		
		try
		{
			if (status == HttpStatus.SC_NOT_FOUND)
			{
				String response = get.getResponseBodyAsString();
				
				// not found. means connection is unavailable
				throw new IOException(response);
			}
			
			throw new IOException("Got unknown status from proxy server: " + status);
		}
		finally
		{
			try { get.releaseConnection(); } catch (Throwable ignored) {}
		}
	}
}
