package com.randomcoder.proxy.server;

import java.io.*;

public interface Endpoint extends Closeable
{
	public InputStream getInputStream() throws IOException;
	
	public OutputStream getOutputStream() throws IOException;
}