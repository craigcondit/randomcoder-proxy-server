package org.randomcoder.proxy.handlers;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.randomcoder.proxy.support.Endpoint;
import org.randomcoder.proxy.support.EndpointEvent;
import org.randomcoder.proxy.support.EndpointTracker;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Handler which displays status.
 */
public class StatusHandler extends AbstractHandler {
  private final String path;
  private final EndpointTracker tracker;

  /**
   * Creates a new status handler.
   *
   * @param path    base URL
   * @param tracker endpoint tracker
   */
  public StatusHandler(String path, EndpointTracker tracker) {
    this.path = path + "/status";
    this.tracker = tracker;
  }

  @Override public void handle(String target, Request baseRequest,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    if (!path.equals(request.getRequestURI())) {
      return;
    }

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("text/plain");

    PrintWriter out = null;
    try {
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

      out = response.getWriter();

      Map<String, Endpoint> endpointMap = tracker.getEndpointMap();
      Map<String, Long> expirationMap = tracker.getExpirationMap();

      out.println("Active sessions:");
      out.println();

      for (Map.Entry<String, Endpoint> ep : endpointMap.entrySet()) {
        String key = ep.getKey();
        out.print(ep.getKey());
        out.print(" => ");
        out.print(ep.getValue());

        Long expiration = expirationMap.get(key);
        if (expiration == null) {
          out.println(" (expiration unknown)");
        } else {
          out.print(" (expires ");
          out.print(df.format(new Date(expiration)));
          out.println(")");
        }
      }

      out.println();
      out.println("Events:");
      out.println();

      for (EndpointEvent event : tracker.getEvents()) {
        out.print(df.format(new Date(event.getTimestamp())));
        out.print(" ");
        out.print(event.getStatus().name());
        out.print(" ");
        out.print(event.getConnectionId());
        out.print(" ");
        out.print(event.getDetails());
        out.println();
      }
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (Throwable ignored) {
      }
    }
    baseRequest.setHandled(true);
  }
}