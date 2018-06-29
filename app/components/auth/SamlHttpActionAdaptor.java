package components.auth;

import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

import controllers.Assets;
import org.apache.commons.lang.StringUtils;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.http.DefaultHttpActionAdapter;
import play.mvc.Result;

/**
 * Action adaptor for showing user a "nice" error page if they are unauthorised.
 */
public class SamlHttpActionAdaptor extends DefaultHttpActionAdapter {

  @Override
  public Result adapt(int code, PlayWebContext context) {
    String responseContent = StringUtils.defaultString(context.getResponseContent());
    if (code == HttpConstants.FORBIDDEN) {
      return redirect(controllers.routes.AuthorisationController.unauthorised());
    } else if (code == HttpConstants.OK && responseContent.contains("onload=\"document.forms[0].submit()\"")) {
      //Hack to intercept the Pac4j self-posting redirect form and inject an external script into it (CSP workaround)
      String jsUrl = controllers.routes.Assets.versioned(new Assets.Asset("javascripts/saml-redirect.js")).toString();
      String fixedResponseContent = responseContent.replace("</body>",
          String.format("<script type=\"text/javascript\" src=\"%s\"></script></body>", jsUrl));

      return ok(fixedResponseContent).as(HttpConstants.HTML_CONTENT_TYPE);
    } else {
      return super.adapt(code, context);
    }
  }
}
