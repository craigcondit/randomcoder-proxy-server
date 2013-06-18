package org.randomcoder.proxy.support;

/**
 * JavaBean which tracks endpoint events.
 */
public class EndpointEvent
{
	/**
	 * Event types.
	 */
	public enum EventType
	{
		/**
		 * Connection created.
		 */
		CONNECT,

		/**
		 * Connection disposed.
		 */
		DISCONNECT,

		/**
		 * Receive completed.
		 */
		RECEIVE_COMPLETE,
		
		/**
		 * Receive error.
		 */
		RECEIVE_ERROR,
		
		/**
		 * Connection expired.
		 */
		EXPIRE
	}

	private String connectionId;
	private String details;
	private EventType status;
	private long timestamp;

	/**
	 * Creates a new event.
	 * 
	 * @param connectionId
	 *            connection ID
	 * @param details
	 *            connection details
	 * @param status
	 *            event status
	 * @param timestamp
	 *            event timestamp
	 */
	public EndpointEvent(String connectionId, String details, EventType status, long timestamp)
	{
		this.connectionId = connectionId;
		this.details = details;
		this.status = status;
		this.timestamp = timestamp;
	}

	/**
	 * Gets the connection ID.
	 * 
	 * @return connection ID
	 */
	public String getConnectionId()
	{
		return connectionId;
	}

	/**
	 * Gets the connection details.
	 * 
	 * @return details
	 */
	public String getDetails()
	{
		return details;
	}

	/**
	 * Gets the status.
	 * 
	 * @return status
	 */
	public EventType getStatus()
	{
		return status;
	}

	/**
	 * Gets the event timestamp.
	 * 
	 * @return timestamp
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
}
