package exceptions;

public class ServiceResponseException extends RuntimeException {

  public ServiceResponseException(String message) {
    super(message);
  }

  public ServiceResponseException(String message, Throwable cause) {
    super(message, cause);
  }

}
