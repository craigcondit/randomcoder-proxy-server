package com.randomcoder.proxy.client;

import org.apache.commons.httpclient.Credentials;

public interface Authenticator
{
	public Credentials getCredentials(String proxyUrl, boolean force);
}