package components.auth;

import components.common.auth.AuthException;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.saml.profile.SAML2Profile;

import java.util.List;

public class SamlAuthorizer implements Authorizer<SAML2Profile> {

  public static final String AUTHORIZER_NAME = "samlAuthorizer";

  @Override
  public boolean isAuthorized(WebContext context, List<SAML2Profile> profiles) throws HttpAction {
    if (profiles.isEmpty()) {
      return false;
    } else if (profiles.size() > 1) {
      throw new AuthException(String.format("Unexpected: Found %d SAML profiles, expected 1", profiles.size()));
    } else {
      return true;
    }
  }

}
