package exceptions;

public class FormStateException extends RuntimeException {

  public FormStateException(String message) {
    super(message);
  }

  public FormStateException(String message, Throwable cause) {
    super(message, cause);
  }

}