package com.randomcoder.proxy.server;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

public class ConnectServlet extends HttpServlet
{
	private static final long serialVersionUID = 5056342868683783827L;

	static
	{
		System.err.println("Ping servlet initialized");
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		// TODO create connection
		
		
		String id = request.getParameter("id");
		
		HttpSession session = request.getSession(true);
		
		response.setContentType("text/plain");
		
		PrintWriter out = null;
		try
		{
			out = response.getWriter();
			out.print("PONG\r\n");
		}
		finally
		{
			try { if (out != null) out.close(); } catch (Throwable ignored) {}
		}
	}
}