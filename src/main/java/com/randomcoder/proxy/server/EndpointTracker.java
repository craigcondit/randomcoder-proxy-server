package com.randomcoder.proxy.server;

import java.util.*;

public class EndpointTracker
{
	private final Map<String, Endpoint> idMap = new HashMap<String, Endpoint>();
	private final Map<String, Long> expireMap = new HashMap<String, Long>();
	
	public EndpointTracker()
	{
	}
	
}