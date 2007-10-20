package com.randomcoder.proxy.client;

import java.io.*;

import org.apache.log4j.Logger;

public class StreamCopyThread extends Thread
{
	private static final Logger logger = Logger.getLogger(StreamCopyThread.class);
	
	private final InputStream input;
	private final OutputStream output;
	
	private long bytesCopied = 0;
	private IOException exception;
	private boolean success = false;
	
	public StreamCopyThread(InputStream input, OutputStream output)
	{
		logger.debug("Copy thread:");
		logger.debug("  FROM: " + input.getClass().getName());
		logger.debug("  TO: " + output.getClass().getName());
		
		this.input = input;
		this.output = output;
	}
	
	@Override
	public void run()
	{
		byte[] buf = new byte[32768];
		
		try
		{
			int c;
			do
			{
				c = input.read(buf, 0, 32768);
				if (c > 0)
				{
					output.write(buf, 0, c);
					output.flush();
					bytesCopied += c;
				}
			}
			while (c >= 0);
			success = true;
		}
		catch (IOException e)
		{
			exception = e;
			success = false;
		}
	}
	
	public boolean isSuccess()
	{
		return success;
	}
	
	public IOException getException()
	{
		return exception;
	}
	
	public long getBytesCopied()
	{
		return bytesCopied;
	}
}