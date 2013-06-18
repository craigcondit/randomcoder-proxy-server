package org.randomcoder.proxy.support;

import java.io.*;
import java.net.Socket;

/**
 * Endpoint implementation that wraps a socket connection.
 */
public class SocketEndpoint implements Endpoint
{
	private final Socket socket;
	private final InputStream input;
	private final OutputStream output;

	/**
	 * Creates a new socket endpoint.
	 * 
	 * @param host
	 *            hostname to connect to
	 * @param port
	 *            port to connect to
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public SocketEndpoint(String host, int port) throws IOException
	{
		socket = new Socket(host, port);
		input = socket.getInputStream();
		output = socket.getOutputStream();
	}

	@Override
	public InputStream getInputStream()
	{
		return input;
	}

	@Override
	public OutputStream getOutputStream()
	{
		return output;
	}

	@Override
	public void close()
	{
		try
		{
			input.close();
		}
		catch (Throwable ignored)
		{
		}
		try
		{
			output.close();
		}
		catch (Throwable ignored)
		{
		}
		try
		{
			socket.close();
		}
		catch (Throwable ignored)
		{
		}
	}

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		buf.append(getClass().getSimpleName());
		buf.append("[");
		buf.append("local=");
		buf.append(socket.getLocalSocketAddress());
		buf.append(",remote=");
		buf.append(socket.getRemoteSocketAddress());
		buf.append("]");
		return buf.toString();
	}
}
