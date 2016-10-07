package components.services.ogels.registration;

public class OgelRegistrationServiceException extends RuntimeException {

  public OgelRegistrationServiceException(String message) {
    super(message);
  }

  public OgelRegistrationServiceException(String message, Throwable cause) {
    super(message, cause);
  }

}