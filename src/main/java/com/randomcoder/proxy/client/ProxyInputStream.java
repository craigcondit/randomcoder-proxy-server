package com.randomcoder.proxy.client;

import java.io.*;
import java.net.URLEncoder;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.randomcoder.proxy.client.config.ProxyConfigurationListener;

/**
 * <code>InputStream</code> implementation which wraps a remote proxied
 * connection.
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
public class ProxyInputStream extends InputStream
{
	private static final Logger logger = Logger.getLogger(ProxyInputStream.class);
	
	private final HttpClient client;
	private final String proxyUrl;
	private final String connectionId;
	private final GetMethod connection;
	private final InputStream inputStream;
	private final ProxyConfigurationListener listener;
	
	/**
	 * Creates a new input stream connected to a remote HTTP proxy.
	 * 
	 * @param client
	 *            HTTP client to use for connections
	 * @param connectionId
	 *            connection id
	 * @param listener
	 *            proxy configuration listener
	 * @throws IOException
	 *             if an error occurs while establishing communications
	 */
	public ProxyInputStream(HttpClient client, String proxyUrl, String connectionId, ProxyConfigurationListener listener)
	throws IOException
	{
		logger.debug("Creating proxy input stream");
		
		this.client = client;
		this.proxyUrl = proxyUrl;
		this.connectionId = connectionId;
		this.listener = listener;
		
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
		int result = inputStream.read();
		if (result >= 0 && listener != null)
			listener.dataReceived(null, 1L);
		
		return result;
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
		int result = inputStream.read(b, off, len);
		
		if (result > 0 && listener != null)
			listener.dataReceived(null, result);
		
		return result;
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		int result = inputStream.read(b);
		
		if (result > 0 && listener != null)
			listener.dataReceived(null, result);
		
		return result;
	}

	@Override
	public synchronized void reset() throws IOException
	{
		inputStream.reset();
	}

	@Override
	public long skip(long n) throws IOException
	{
		long result = inputStream.skip(n);

		if (result > 0 && listener != null)
			listener.dataReceived(null, result);
		
		return result;
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