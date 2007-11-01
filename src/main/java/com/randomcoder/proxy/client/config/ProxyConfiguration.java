package com.randomcoder.proxy.client.config;

import java.io.Serializable;
import java.net.*;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import com.randomcoder.proxy.client.validation.ValidationResult;

/**
 * HTTP proxy configuration bean.
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
public class ProxyConfiguration implements Serializable, Comparable<ProxyConfiguration>, Cloneable
{
	private static final long serialVersionUID = 8946339597124804174L;
	
	private String name;
	private String proxyUrl;
	private String username;
	private String remoteHost;
	private Integer remotePort;
	private Integer localPort;
	
	/**
	 * Gets the name of this configuration item.
	 * 
	 * @return configuration name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Sets the name of this configuration item.
	 * 
	 * @param name
	 *            configuration name
	 */
	public void setName(String name)
	{
		this.name = StringUtils.trimToNull(name);
	}
	
	/**
	 * Gets the URL of the remote proxy.
	 * 
	 * @return proxy url
	 */
	public String getProxyUrl()
	{
		return proxyUrl;
	}

	/**
	 * Sets the URL of the remote proxy.
	 * 
	 * @param proxyUrl
	 *            proxy url
	 */
	public void setProxyUrl(String proxyUrl)
	{
		this.proxyUrl = StringUtils.trimToNull(proxyUrl);
	}
	
	/**
	 * Gets the username associated with this proxy.
	 * 
	 * @return username
	 */
	public String getUsername()
	{
		return username;
	}
	
	/**
	 * Sets the username associated with this proxy.
	 * 
	 * @param username
	 *            username
	 */
	public void setUsername(String username)
	{
		this.username = username;
	}

	/**
	 * Gets the remote host to tunnel to.
	 * 
	 * @return remote host
	 */
	public String getRemoteHost()
	{
		return remoteHost;
	}
	
	/**
	 * Sets the remote host to tunnel to.
	 * 
	 * @param remoteHost
	 *            remote host
	 */
	public void setRemoteHost(String remoteHost)
	{
		this.remoteHost = StringUtils.trimToNull(remoteHost);
	}
	
	/**
	 * Gets the remote port to tunnel to.
	 * 
	 * @return remote port
	 */
	public Integer getRemotePort()
	{
		return remotePort;
	}
	
	/**
	 * Sets the remote port to tunnel to.
	 * 
	 * @param remotePort
	 *            remote port
	 */
	public void setRemotePort(Integer remotePort)
	{
		if (remotePort != null && (remotePort < 1 || remotePort > 65535))
			remotePort = null;
		
		this.remotePort = remotePort;
	}
	
	/**
	 * Gets the local port to listen on.
	 * 
	 * @return local port
	 */
	public Integer getLocalPort()
	{
		return localPort;
	}
	
	/**
	 * Sets the local port to listen on.
	 * 
	 * @param localPort
	 *            local port
	 */
	public void setLocalPort(Integer localPort)
	{
		if (localPort != null && (localPort < 1 || localPort > 65535))
			localPort = null;
		
		this.localPort = localPort;
	}

	/**
	 * Compares this configuration to another configuration.
	 * 
	 * @param obj
	 *            object to compare to
	 * @return -1, 0, or 1 if this object is less than, equal, or greater than
	 *         the specified object
	 */
	public int compareTo(ProxyConfiguration obj)
	{
		String s1 = (name == null) ? "" : name.trim();
		String s2 = (obj.name == null) ? "" : obj.name.trim();
		
		return s1.compareToIgnoreCase(s2);
	}
	
	/**
	 * Validates this object.
	 * 
	 * @param peers
	 *            list of other objects which are also loaded
	 * @return list of validation results
	 */
	public List<ValidationResult> validate(List<ProxyConfiguration> peers)
	{
		List<ValidationResult> results = new ArrayList<ValidationResult>();
		
		if (name == null)
			results.add(new ValidationResult("name", "Connection name is required."));
		else
		{
			for (ProxyConfiguration peer : peers)
			{
				if ((peer != this) && name.equals(peer.getName()))
				{
					results.add(new ValidationResult("name", "Another connection with this name already exists."));
					break;
				}
			}
		}
		
		
		if (proxyUrl == null)
			results.add(new ValidationResult("proxyUrl", "Proxy URL is required."));
		else
		{
			URL url = null;
			try
			{
				url = new URL(proxyUrl);
				if (!("http".equals(url.getProtocol())) && !("https".equals(url.getProtocol())))
					url = null;
			}
			catch (MalformedURLException e)
			{
				url = null;
			}
			
			if (url == null)
				results.add(new ValidationResult("proxyUrl", "Proxy URL is invalid."));
		}
		
		if (remoteHost == null)
			results.add(new ValidationResult("remoteHost", "Remote host is required."));
		else
		{
			try
			{
				InetAddress addr = InetAddress.getByName(remoteHost);
			}
			catch (UnknownHostException e)
			{
				results.add(new ValidationResult("remoteHost", "Remote host is unreachable."));
			}
		}
		
		if (remotePort == null)
			results.add(new ValidationResult("remotePort", "Remote port is required."));
		
		if (localPort == null)
			results.add(new ValidationResult("localPort", "Local port is required."));
		
		return results;
	}

	/**
	 * Clones this object into a new one.
	 * 
	 * @return deep copy of this object
	 */
	@Override
	public ProxyConfiguration clone()
	{
		ProxyConfiguration proxy = new ProxyConfiguration();
		proxy.name = name;
		proxy.proxyUrl = proxyUrl;
		proxy.username = username;
		proxy.remoteHost = remoteHost;
		proxy.remotePort = remotePort;
		proxy.localPort = localPort;
		
		return proxy;
	}
}