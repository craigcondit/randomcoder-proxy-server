package org.randomcoder.proxy.support;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * Endpoint tracker which watches Endpoint instances and cleans them up after a
 * specified period of inactivity.
 * 
 * <pre>
 * Copyright (c) 2007-2010, Craig Condit. All rights reserved.
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
public class EndpointTracker
{
	/**
	 * Logger instance.
	 */
	protected static final Logger logger = Logger.getLogger(EndpointTracker.class);
	
	/**
	 * Map of ids to endpoints.
	 */
	protected final ConcurrentHashMap<String, Endpoint> endpointMap
		= new ConcurrentHashMap<String, Endpoint>();
	
	/**
	 * Map of ids to expiration times.
	 */
	protected final ConcurrentHashMap<String, Long> expirationMap
		= new ConcurrentHashMap<String, Long>();

	/**
	 * Maximum idle time in milliseconds;
	 */
	protected final long maxIdle;
	
	/**
	 * Time to sleep between eviction runs in milliseconds.
	 */
	protected final long evictionFrequency;	

	private final ReaperThread reaperThread;

	/**
	 * Creats a new endpoint tracker using default values.
	 */
	public EndpointTracker()
	{
		this(30000L, 30000L);
	}

	/**
	 * Creates a new endpoint tracker.
	 * 
	 * @param maxIdle
	 *          maximum time before idle threads are killed (in milliseconds)
	 * @param evictionFrequency
	 *          how often to perform evictions
	 */
	public EndpointTracker(long maxIdle, long evictionFrequency)
	{
		this.maxIdle = maxIdle;
		this.evictionFrequency = evictionFrequency;
		reaperThread = new ReaperThread();
		reaperThread.start();
		logger.info("Endpoint tracker initialized");
	}

	public void destroy()
	{
		logger.info("Endpoint tracker shutting down...");
		
		reaperThread.shutdown();
		try { reaperThread.join(30000); } catch (InterruptedException ignored) {}

		int count = 0;
		
		// make sure all referenced connections are closed
		for (Map.Entry<String, Endpoint> entry : endpointMap.entrySet())
		{
			String id = entry.getKey();
			expirationMap.remove(id);
			Endpoint endpoint = endpointMap.remove(id);
			if (endpoint != null)
			{
				try { endpoint.close(); } catch (Throwable ignored) {}
				count++;
			}
		}
		
		logger.info("Endpoint tracker shutdown, " + count + " endpoints closed");
	}

	/**
	 * Adds a new endpoint to the tracker.
	 * 
	 * @param endpoint
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
		try { if (endpoint != null) endpoint.close(); } catch (Throwable ignored) {}
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
		long timeout = System.currentTimeMillis() + maxIdle;
		Long oldTimeout = expirationMap.replace(id, timeout);
		
		logger.debug("Refresh [" + id + "]: old=" + oldTimeout + ",new=" + timeout);

		return (oldTimeout != null);
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
		return endpointMap.get(id);
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
					
					logger.debug("Checking for stale connections, time = " + now);
					
					// walk object map
					for (Map.Entry<String, Long> entry : expirationMap.entrySet())
					{
						if (entry.getValue() <= now)
						{
							// remove stale object
							String id = entry.getKey();							
							expirationMap.remove(id);
							Endpoint endpoint = endpointMap.remove(id);
							if (endpoint != null)
							{
								logger.info("Closing stale connection with ID " + id);
								try { endpoint.close(); } catch (Throwable ignored) {}
							}							
						}
					}
					
					logger.debug("Done checking for stale connections");
					
					// sleep until next round
					try { Thread.sleep(evictionFrequency); } catch (InterruptedException ignored) {}					
				}
				catch (Throwable t)
				{
					// defensive catch to avoid thread death
					logger.error("Caught exception", t);
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
