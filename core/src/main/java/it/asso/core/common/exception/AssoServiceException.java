package it.asso.core.common.exception;


/**
 * ProjServiceException
 *
 */
public class AssoServiceException extends Exception {

	private static final long serialVersionUID = -7783277535362243756L;

/**
   * @param message
   */
  public AssoServiceException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public AssoServiceException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public AssoServiceException(String message, Throwable cause) {
    super(message, cause);
  }

}
