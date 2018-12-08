package org.randomcoder.proxy.support;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Endpoint interface. An endpoint is an encapsulation of an {@link InputStream}
 * / {@link OutputStream} pair.
 */
public interface Endpoint extends Closeable {
  /**
   * Gets the input stream wrapped by this endpoint. Implementations must
   * ensure that this method may be called multiple times.
   *
   * @return input stream
   * @throws IOException if an I/O error occurs
   */
  public InputStream getInputStream() throws IOException;

  /**
   * Gets the output stream wrapped by this endpoint. Implementations must
   * ensure that this method may be called multiple times.
   *
   * @return output stream
   * @throws IOException if an I/O error occurs
   */
  public OutputStream getOutputStream() throws IOException;
}
