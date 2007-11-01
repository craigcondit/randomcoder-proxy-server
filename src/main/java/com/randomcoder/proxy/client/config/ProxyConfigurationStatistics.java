package com.randomcoder.proxy.client.config;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.randomcoder.proxy.client.*;

/**
 * Class to track statistics and status of a proxy connection.
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
public class ProxyConfigurationStatistics extends ProxyConfiguration implements ProxyConfigurationListener
{
	private static final Logger logger = Logger.getLogger(ProxyConfigurationStatistics.class);
	
	private boolean connected;
	private boolean starting;
	private boolean stopping;
	private int activeCount;
	private long bytesReceived;
	private long bytesSent;
	private ProxyClient proxyClient;
	private ListenThread listenThread;
	private final ConcurrentLinkedQueue<ProxyConfigurationListener> listeners = new ConcurrentLinkedQueue<ProxyConfigurationListener>();
	
	private static final long serialVersionUID = 7675553419493897275L;

	/**
	 * Wraps the given proxy configuration in a stats tracker.
	 * 
	 * @param config
	 *            proxy configuration to wrap
	 */
	public ProxyConfigurationStatistics(ProxyConfiguration config)
	{
		super(config);
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param source
	 *            source object to clone
	 */
	public ProxyConfigurationStatistics(ProxyConfigurationStatistics source)
	{
		super((ProxyConfiguration) source);
		connected = source.connected;
		activeCount = source.activeCount;
		bytesReceived = source.bytesReceived;
		bytesSent = source.bytesSent;
	}
	
	/**
	 * Clones this object.
	 * 
	 * @return clone
	 */
	@Override
	public ProxyConfigurationStatistics clone()
	{
		return new ProxyConfigurationStatistics(this);
	}
	
	public void addProxyConfigurationListener(ProxyConfigurationListener listener)
	{
		for (ProxyConfigurationListener existing : listeners)
			if (existing == listener)
				return;
		
		listeners.add(listener);
	}
	
	public void removeProxyConfigurationListener(ProxyConfigurationListener listener)
	{
		for (Iterator<ProxyConfigurationListener> it = listeners.iterator(); it.hasNext();)
			if (listener == it.next())
				it.remove();
	}
	
	public boolean isConnected()
	{
		return connected;
	}
	
	public boolean isStarting()
	{
		return starting;
	}
	
	public boolean isStopping()
	{
		return stopping;
	}
	
	public int getActiveCount()
	{
		return activeCount;
	}
	
	public long getBytesReceived()
	{
		return bytesReceived;
	}
	
	public long getBytesSent()
	{
		return bytesSent;
	}
	
	public synchronized void connect(Authenticator auth)
	{
		if (listenThread != null)
			disconnect();
		
		connectionSetupStarting(this);
		
		proxyClient = new ProxyClient(name, proxyUrl, username, remoteHost, remotePort, localPort, auth, this);
		
		listenThread = new ListenThread(proxyClient);
		listenThread.start();
		
		connectionSetup(this);
	}
	
	public synchronized void disconnect()
	{
		connectionTeardownStarting(this);
		
		if (listenThread != null)
		{
			listenThread.shutdown();
			try { listenThread.join(); } catch (Exception ignored) {}
			logger.debug("Stopped listen thread");
		}
		
		if (proxyClient != null)
			proxyClient = null;
		
		connectionTeardown(this);
	}
	
	protected static final class ListenThread extends Thread
	{
		private final ProxyClient client;
		
		public ListenThread(ProxyClient client)
		{
			super("Proxy Thread");
			this.client = client;
		}
		
		@Override
		public void run()
		{
			try
			{
				client.listen();
			}
			catch (IOException e)
			{
				logger.error("Caught exception while listening", e);
			}
		}
		
		public void shutdown()
		{
			client.shutdown();
		}
	}

	public void configSaved(List<ProxyConfiguration> config)
	{
		for (ProxyConfigurationListener listener : listeners)
			listener.configSaved(config);
	}

	public void connectionClosed(ProxyConfigurationStatistics config)
	{
		activeCount--;
		
		for (ProxyConfigurationListener listener : listeners)
			listener.connectionClosed(this);
	}

	public void connectionOpened(ProxyConfigurationStatistics config)
	{
		activeCount++;
		
		for (ProxyConfigurationListener listener : listeners)
			listener.connectionOpened(this);
	}

	public void connectionSetup(ProxyConfigurationStatistics config)
	{
		bytesSent = 0L;
		bytesReceived = 0L;
		activeCount = 0;
		connected = true;
		stopping = false;
		starting = false;
		
		for (ProxyConfigurationListener listener : listeners)
			listener.connectionSetup(this);
	}

	public void connectionTeardown(ProxyConfigurationStatistics config)
	{
		bytesSent = 0L;
		bytesReceived = 0L;
		activeCount = 0;
		connected = false;
		stopping = false;
		starting = false;
		
		for (ProxyConfigurationListener listener : listeners)
			listener.connectionTeardown(this);
	}

	public void connectionSetupStarting(ProxyConfigurationStatistics config)
	{
		starting = true;
		connected = false;
		stopping = false;
		
		for (ProxyConfigurationListener listener : listeners)
			listener.connectionSetupStarting(this);
	}

	public void connectionTeardownStarting(ProxyConfigurationStatistics config)
	{
		starting = false;
		connected = false;
		stopping = false;
		
		for (ProxyConfigurationListener listener : listeners)
			listener.connectionTeardownStarting(this);
	}

	public void dataReceived(ProxyConfigurationStatistics config, long bytes)
	{
		bytesReceived += bytes;
		
		for (ProxyConfigurationListener listener : listeners)
			listener.dataReceived(this, bytes);
	}

	public void dataSent(ProxyConfigurationStatistics config, long bytes)
	{
		bytesSent += bytes;
		
		for (ProxyConfigurationListener listener : listeners)
			listener.dataSent(this, bytes);
	}	
}