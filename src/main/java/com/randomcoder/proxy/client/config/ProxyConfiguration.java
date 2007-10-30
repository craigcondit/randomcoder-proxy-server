package com.randomcoder.proxy.client.config;

import java.io.Serializable;

public class ProxyConfiguration implements Serializable, Comparable<ProxyConfiguration>
{
	private static final long serialVersionUID = 8946339597124804174L;
	
	private String name;
	private String proxyUrl;
	private String username;
	private String password;
	private String remoteHost;
	private int remotePort;
	private int localPort;
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getProxyUrl()
	{
		return proxyUrl;
	}
	
	public void setProxyUrl(String proxyUrl)
	{
		this.proxyUrl = proxyUrl;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public String getRemoteHost()
	{
		return remoteHost;
	}
	
	public void setRemoteHost(String remoteHost)
	{
		this.remoteHost = remoteHost;
	}
	
	public int getRemotePort()
	{
		return remotePort;
	}
	
	public void setRemotePort(int remotePort)
	{
		this.remotePort = remotePort;
	}
	
	public int getLocalPort()
	{
		return localPort;
	}
	
	public void setLocalPort(int localPort)
	{
		this.localPort = localPort;
	}

	public int compareTo(ProxyConfiguration obj)
	{
		String s1 = (name == null) ? "" : name.trim();
		String s2 = (obj.name == null) ? "" : obj.name.trim();
		
		return s1.compareToIgnoreCase(s2);
	}
}