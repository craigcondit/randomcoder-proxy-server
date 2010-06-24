package com.randomcoder.proxy.support;

import java.io.*;
import java.net.Socket;

/**
 * Endpoint implementation that wraps a socket connection.
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
public class SocketEndpoint implements Endpoint
{
	private final Socket socket;
	private final InputStream input;
	private final OutputStream output;
	
	/**
	 * Creates a new socket endpoint.
	 * 
	 * @param host
	 *          hostname to connect to
	 * @param port
	 *          port to connect to
	 * @throws IOException
	 *           if an I/O error occurs
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
		try { input.close(); } catch (Throwable ignored) {}
		try { output.close(); } catch (Throwable ignored) {}
		try { socket.close(); } catch (Throwable ignored) {}
	}
}