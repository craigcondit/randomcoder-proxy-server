package org.randomcoder.proxy.handlers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.randomcoder.proxy.support.CurrentUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Authentication verification handler. Sends OK if authentication succeeds,
 * which should happen if this handler is called after {@link BasicAuthHandler}.
 */
public class AuthHandler extends AbstractHandler {
  private static final Logger logger = LogManager.getLogger(AuthHandler.class);

  private final String path;

  /**
   * Creates a new authentication handler.
   *
   * @param path base URL
   */
  public AuthHandler(String path) {
    this.path = path + "/auth";
  }

  @Override public void handle(String target, Request baseRequest,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    if (!path.equals(request.getRequestURI())) {
      return;
    }

    if (logger.isDebugEnabled())
      logger.debug("Auth: user=" + CurrentUser.get());

    response.setContentType("text/plain");
    response.setContentLength(4);

    PrintWriter out = null;
    try {
      out = response.getWriter();
      out.print("OK\r\n");
    } finally {
      try {
        if (out != null)
          out.close();
      } catch (Throwable ignored) {
      }
    }

    baseRequest.setHandled(true);
  }
}
