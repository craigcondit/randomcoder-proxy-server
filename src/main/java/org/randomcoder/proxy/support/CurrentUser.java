package org.randomcoder.proxy.support;

/**
 * Context object which allows getting and setting the currently logged-in user.
 */
public class CurrentUser {
  private static final ThreadLocal<String> currentUser =
      new ThreadLocal<String>();

  /**
   * Logs the current user out.
   */
  public static void logout() {
    currentUser.set(null);
  }

  /**
   * Logs in.
   *
   * @param user user to login
   */
  public static void login(String user) {
    currentUser.set(user);
  }

  /**
   * Gets the current user.
   *
   * @return current user
   */
  public static String get() {
    return currentUser.get();
  }
}
