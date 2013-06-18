package org.randomcoder.proxy.support;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * Endpoint tracker which watches Endpoint instances and cleans them up after a
 * specified period of inactivity.
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
	protected final ConcurrentHashMap<String, Endpoint> endpointMap = new ConcurrentHashMap<String, Endpoint>();

	/**
	 * Map of ids to expiration times.
	 */
	protected final ConcurrentHashMap<String, Long> expirationMap = new ConcurrentHashMap<String, Long>();

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
		this(300000L, 30000L);
	}

	/**
	 * Creates a new endpoint tracker.
	 * 
	 * @param maxIdle
	 *            maximum time before idle threads are killed (in milliseconds)
	 * @param evictionFrequency
	 *            how often to perform evictions
	 */
	public EndpointTracker(long maxIdle, long evictionFrequency)
	{
		this.maxIdle = maxIdle;
		this.evictionFrequency = evictionFrequency;
		reaperThread = new ReaperThread();
		reaperThread.start();
		logger.info("Endpoint tracker initialized");
	}

	/**
	 * Destroys the tracker.
	 */
	public void destroy()
	{
		logger.info("Endpoint tracker shutting down...");

		reaperThread.shutdown();
		try
		{
			reaperThread.join(30000);
		}
		catch (InterruptedException ignored)
		{
		}

		int count = 0;

		// make sure all referenced connections are closed
		for (Map.Entry<String, Endpoint> entry : endpointMap.entrySet())
		{
			String id = entry.getKey();
			expirationMap.remove(id);
			Endpoint endpoint = endpointMap.remove(id);
			if (endpoint != null)
			{
				try
				{
					endpoint.close();
				}
				catch (Throwable ignored)
				{
				}
				count++;
			}
		}

		logger.info("Endpoint tracker shutdown, " + count + " endpoints closed");
	}

	/**
	 * Adds a new endpoint to the tracker.
	 * 
	 * @param endpoint
	 *            endpoint to add
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
	 *            unique identifier of endpoint to remove
	 */
	public void remove(String id)
	{
		expirationMap.remove(id);
		Endpoint endpoint = endpointMap.remove(id);
		try
		{
			if (endpoint != null)
				endpoint.close();
		}
		catch (Throwable ignored)
		{
		}
	}

	/**
	 * Refreshes an endpoint's timeout value, typically in response to activity
	 * or a keep-alive request.
	 * 
	 * @param id
	 *            unique identifier of endpoint to refresh
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
	 *            unique identifier of endpoint to retrieve
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
								try
								{
									endpoint.close();
								}
								catch (Throwable ignored)
								{
								}
							}
						}
					}

					logger.debug("Done checking for stale connections");

					// sleep until next round
					try
					{
						Thread.sleep(evictionFrequency);
					}
					catch (InterruptedException ignored)
					{
					}
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
