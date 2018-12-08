package org.randomcoder.proxy.handlers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.randomcoder.proxy.support.CurrentUser;
import org.randomcoder.proxy.support.EndpointTracker;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handler which acts as a keepalive.
 */
public class PingHandler extends AbstractHandler {
  private static final Logger logger = LogManager.getLogger(PingHandler.class);

  private final String path;
  private final EndpointTracker tracker;

  /**
   * Creates a new ping handler.
   *
   * @param path    base URL
   * @param tracker endpoint tracker
   */
  public PingHandler(String path, EndpointTracker tracker) {
    this.path = path + "/ping";
    this.tracker = tracker;
  }

  @Override public void handle(String target, Request baseRequest,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    if (!path.equals(request.getRequestURI())) {
      return;
    }

    String id = request.getParameter("id");
    boolean active = tracker.refresh(id);

    if (logger.isDebugEnabled())
      logger.debug(
          "Ping [" + id + "]: user=" + CurrentUser.get() + ", status=" + (
              active ? "active" : "closed"));

    response.setStatus(
        active ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
    response.setContentType("text/plain");

    PrintWriter out = null;
    try {
      out = response.getWriter();
      out.print(active ? "ACTIVE\r\n" : "CLOSED\r\n");
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
