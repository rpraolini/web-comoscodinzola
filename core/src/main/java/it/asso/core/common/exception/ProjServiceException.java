package it.asso.core.common.exception;


/**
 * ProjServiceException
 *
 */
public abstract class ProjServiceException extends Exception  {

	private static final long serialVersionUID = -8593756297735346451L;

/**
   * @param message
   */
  public ProjServiceException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public ProjServiceException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public ProjServiceException(String message, Throwable cause) {
    super(message, cause);
  }

}
