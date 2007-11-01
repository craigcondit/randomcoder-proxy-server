package com.randomcoder.proxy.client.config;

import java.util.List;

abstract public class ProxyConfigurationListenerAdapter implements ProxyConfigurationListener
{

	public void configSaved(List<ProxyConfiguration> config)
	{
	}

	public void connectionClosed(ProxyConfigurationStatistics config)
	{
	}

	public void connectionOpened(ProxyConfigurationStatistics config)
	{
	}

	public void connectionSetup(ProxyConfigurationStatistics config)
	{
	}

	public void connectionSetupStarting(ProxyConfigurationStatistics config)
	{
	}

	public void connectionTeardown(ProxyConfigurationStatistics config)
	{
	}

	public void connectionTeardownStarting(ProxyConfigurationStatistics config)
	{
	}

	public void dataReceived(ProxyConfigurationStatistics config, long bytes)
	{
	}

	public void dataSent(ProxyConfigurationStatistics config, long bytes)
	{
	}
}