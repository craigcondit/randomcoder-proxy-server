package com.randomcoder.proxy.server;

import java.io.Serializable;

/**
 * Command object which holds a host and port.
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
public class ConnectCommand implements Serializable
{
	private static final long serialVersionUID = -4002961851534625656L;

	private String host;
	private int port;

	/**
	 * Gets the host to connect to.
	 * 
	 * @return hostname
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Sets the host to connect to.
	 * 
	 * @param host
	 *          hostname
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * Gets the port to connect to.
	 * 
	 * @return port number
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * Sets the port to connect to.
	 * 
	 * @param port
	 *          port number
	 */
	public void setPort(int port)
	{
		this.port = port;
	}
}