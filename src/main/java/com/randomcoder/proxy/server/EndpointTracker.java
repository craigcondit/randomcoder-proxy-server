package com.randomcoder.proxy.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.*;

/**
 * Endpoint tracker which watches Endpoint instances and cleans them up after a
 * specified period of inactivity.
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
public class EndpointTracker implements InitializingBean, DisposableBean
{
	protected static final Logger log = Logger.getLogger(EndpointTracker.class);
	
	/**
	 * Map of ids to endpoints
	 */
	protected final ConcurrentHashMap<String, Endpoint> endpointMap
		= new ConcurrentHashMap<String, Endpoint>();
	
	/**
	 * Map of ids to expiration times
	 */
	protected final ConcurrentHashMap<String, Long> expirationMap
		= new ConcurrentHashMap<String, Long>();

	/**
	 * Maximum idle time in milliseconds
	 */
	protected long maxIdle = 30000L;
	
	/**
	 * How often stale objects are purged
	 */
	protected long evictionFrequency = 30000L;
	
	private ReaperThread reaperThread = null;

	/**
	 * Sets the maximum before inactive endpoints will be closed.
	 * 
	 * @param maxIdle
	 *          inactivity time in milliseconds
	 */
	public void setMaxIdle(long maxIdle)
	{
		this.maxIdle = maxIdle;
	}

	/**
	 * Sets the amount of time between eviction runs.
	 * 
	 * @param evictionFrequency
	 *          frequency between eviction runs in milliseconds
	 */
	public void setEvictionFrequency(long evictionFrequency)
	{
		this.evictionFrequency = evictionFrequency;
	}
	
	/**
	 * Initializes the endpoint tracker.
	 */
	public void afterPropertiesSet() throws Exception
	{
		reaperThread = new ReaperThread();
		reaperThread.start();
	}

	/**
	 * Shuts down the endpoint tracker, closing all active endpoints.
	 */
	public void destroy()
	{
	// TODO stop reaper thread

	}

	/**
	 * Adds a new endpoint to the tracker.
	 * 
	 * @param endpoing
	 *          endpoint to add
	 * @return unique identifier
	 */
	public String add(Endpoint endpoint)
	{
		String id = UUID.randomUUID().toString();
		
		endpointMap.put(id, endpoint);
		expirationMap.put(id, System.currentTimeMillis() + maxIdle);
		
		return id;
	}

	/**
	 * Removes an existing endpoint from the tracker.
	 * 
	 * @param id
	 *          unique identifier of endpoint to remove
	 */
	public void remove(String id)
	{
		expirationMap.remove(id);
		Endpoint endpoint = endpointMap.remove(id);
		if (endpoint != null)
		{
			try { endpoint.close(); } catch (Throwable ignored) {}
		}
	}

	/**
	 * Refreshes an endpoint's timeout value, typically in response to activity or
	 * a keep-alive request.
	 * 
	 * @param id
	 *          unique identifier of endpoint to refresh
	 * @return <code>true</code> if endpoint was still active
	 */
	public boolean refresh(String id)
	{
		return (expirationMap.replace(id, System.currentTimeMillis() + maxIdle) != null);
	}

	/**
	 * Gets an endpoint by id.
	 * 
	 * @param id
	 *          unique identifier of endpoint to retrieve
	 * @return endpoint, or <code>null</code> if not found
	 */
	public Endpoint getEndpoint(String id)
	{
		return null;
	}
	
	private final class ReaperThread extends Thread
	{
		private volatile boolean shutdown = false;
		
		/**
		 * Creates a new reaper thread.
		 */
		public ReaperThread()
		{
			super("Endpoint reaper");
		}
		
		@Override
		public void run()
		{
			while (!shutdown)
			{
				try
				{

					long now = System.currentTimeMillis();
					
					// walk object map
					for (Map.Entry<String, Long> entry : expirationMap.entrySet())
					{
						if (entry.getValue() >= now)
						{
							// remove stale object
							String id = entry.getKey();							
							expirationMap.remove(id);
							Endpoint endpoint = endpointMap.remove(id);
							if (endpoint != null)
							{
								try { endpoint.close(); } catch (Throwable ignored) {}
							}							
						}
					}
					
					// sleep until next round
					try { Thread.sleep(evictionFrequency); } catch (InterruptedException ignored) {}					
				}
				catch (Throwable t)
				{
					// defensive catch to avoid thread death
					log.error("Caught exception", t);
				}			
			}
		}

		/**
		 * Requests that this thread be shutdown.
		 */
		public void shutdown()
		{
			shutdown = true;
			interrupt();
		}
	}
}