package com.randomcoder.proxy.server;

import javax.servlet.http.*;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

public class PingController extends AbstractCommandController
{

	@Override
	protected ModelAndView handle(
			HttpServletRequest request, HttpServletResponse response,
			Object command, BindException errors)
	throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

}
