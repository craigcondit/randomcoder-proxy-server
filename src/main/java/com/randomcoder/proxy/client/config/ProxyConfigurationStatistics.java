package com.randomcoder.proxy.client.config;

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
public class ProxyConfigurationStatistics extends ProxyConfiguration
{
	private boolean connected;
	private int activeCount;
	private long bytesReceived;
	private long bytesSent;
	
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
	
	public boolean isConnected()
	{
		return connected;
	}

	public void setConnected(boolean connected)
	{
		this.connected = connected;
	}
	
	public int getActiveCount()
	{
		return activeCount;
	}
	
	public void setActiveCount(int activeCount)
	{
		this.activeCount = activeCount;
	}
	
	public long getBytesReceived()
	{
		return bytesReceived;
	}
	
	public void setBytesReceived(long bytesReceived)
	{
		this.bytesReceived = bytesReceived;
	}
	
	public long getBytesSent()
	{
		return bytesSent;
	}
	
	public void setBytesSent(long bytesSent)
	{
		this.bytesSent = bytesSent;
	}
}