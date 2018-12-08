package org.randomcoder.proxy.handlers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.security.UnixCrypt;
import org.randomcoder.proxy.support.CurrentUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * BASIC authentication handler.
 */
public class BasicAuthHandler extends AbstractHandler {
  private static final Logger logger =
      LogManager.getLogger(BasicAuthHandler.class);

  private static final String REALM = "Proxy";

  private final Map<String, String> userMap;

  /**
   * Creates a new BASIC auth handler.
   *
   * @param passwdFile password file
   * @throws IOException if an error occurs
   */
  public BasicAuthHandler(File passwdFile) throws IOException {
    Map<String, String> map = new HashMap<String, String>();

    FileReader fr = null;
    BufferedReader br = null;
    try {
      fr = new FileReader(passwdFile);
      br = new BufferedReader(fr);
      String line = null;
      while ((line = br.readLine()) != null) {
        String[] parts = line.split(":");
        if (parts.length >= 2) {
          map.put(parts[0], parts[1]);
        }
      }
    } finally {
      if (br != null)
        try {
          br.close();
        } catch (Exception ignored) {
        }
      if (fr != null)
        try {
          fr.close();
        } catch (Exception ignored) {
        }
    }

    logger.info("Initialized user store with " + map.size() + " entries");
    userMap = Collections.unmodifiableMap(map);
  }

  @Override public void handle(String target, Request baseRequest,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String username = getUser(request.getHeader("Authorization"));
    if (username == null) {
      CurrentUser.logout();
      response.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      baseRequest.setHandled(true);
      return;
    }
    CurrentUser.login(username);
  }

  private String getUser(String auth) {
    try {
      if (auth == null) {
        return null;
      }

      String[] parts = auth.split(" ");
      if (parts.length < 2) {
        return null;
      }

      if (!("basic".equals(parts[0].toLowerCase(Locale.US)))) {
        return null;
      }

      String token = B64Code.decode(parts[1], Charset.forName("UTF-8"));
      String[] userpass = token.split(":");
      if (userpass.length != 2) {
        return null;
      }

      String hash = userMap.get(userpass[0]);
      if (hash == null) {
        return null;
      }

      if (!(hash.equals(UnixCrypt.crypt(userpass[1], hash)))) {
        return null;
      }
      return userpass[0];
    } catch (Exception e) {
      logger.warn("Invalid auth token received", e);
      return null;
    }
  }
}
