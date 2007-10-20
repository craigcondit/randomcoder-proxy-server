package com.randomcoder.proxy.server;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

public class PingServlet extends HttpServlet
{
	private static final long serialVersionUID = 5056342868683783827L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
	{
		// TODO mark connection as active
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