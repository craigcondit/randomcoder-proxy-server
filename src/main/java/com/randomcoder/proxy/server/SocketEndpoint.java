package com.randomcoder.proxy.server;

import java.io.*;
import java.net.Socket;

public class SocketEndpoint implements Endpoint
{
	private final Socket socket;
	private final InputStream input;
	private final OutputStream output;
	
	public SocketEndpoint(String host, int port) throws IOException
	{
		socket = new Socket(host, port);
		input = socket.getInputStream();
		output = socket.getOutputStream();
	}
	
	public InputStream getInputStream() throws IOException
	{
		return input;
	}

	public OutputStream getOutputStream() throws IOException
	{
		return output;
	}

	public void close() throws IOException
	{
		try { input.close(); } catch (Throwable ignored) {}
		try { output.close(); } catch (Throwable ignored) {}
		try { socket.close(); } catch (Throwable ignored) {}
	}
}