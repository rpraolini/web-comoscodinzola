package it.asso.core.common.exception;

import java.io.IOException;

/**
 * AuthenticationException
 *
 */
public class AutenticazioneException extends ProjServiceException {

  /**
 * 
 */
private static final long serialVersionUID = -8425533675791637654L;

  /**
   * @param message
 * @throws IOException 
   */
  public AutenticazioneException(String message) throws IOException {
    super(message);
  }

  /**
   * @param cause
   */
  public AutenticazioneException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public AutenticazioneException(String message, Throwable cause) {
    super(message, cause);
  }

}
