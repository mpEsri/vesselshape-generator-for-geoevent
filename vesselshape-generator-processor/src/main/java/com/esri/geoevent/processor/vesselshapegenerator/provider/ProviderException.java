package com.esri.geoevent.processor.vesselshapegenerator.provider;

/**
 * Provider exception.
 */
public class ProviderException extends Exception {

  /**
   * Creates a new instance of <code>ProviderException</code> without detail
   * message.
   */
  public ProviderException() {
  }

  /**
   * Constructs an instance of <code>ProviderException</code> with the specified
   * detail message.
   *
   * @param msg the detail message.
   */
  public ProviderException(String msg) {
    super(msg);
  }

  /**
   * Constructs an instance of <code>ProviderException</code> with the specified
   * detail message.
   *
   * @param msg the detail message.
   * @param cause the cause
   */
  public ProviderException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
