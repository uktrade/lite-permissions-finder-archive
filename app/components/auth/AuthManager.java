package components.auth;

import com.google.inject.Inject;
import play.mvc.Http;

public class AuthManager {

  private static String CONTEXT_ARG_NAME = "auth_manager";

  private static final String ID_ATTRIBUTE = "ID";
  private static final String EMAIL_ADDRESS_ATTRIBUTE = "PRIMARY_EMAIL_ADDRESS";
  private static final String FORENAME_ATTRIBUTE = "FORENAME";
  private static final String SURNAME_ATTRIBUTE = "SURNAME";

  @Inject
  public AuthManager() {
  }

  /**
   * Sets this instance as an argument on the given context, for later retrieval in views. This can be removed when
   * DI is supported in views.
   * @param context Context to set arg on.
   */
  public void setAsContextArgument(Http.Context context) {
    context.args.put(CONTEXT_ARG_NAME, this);
  }

}
