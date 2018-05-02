package components.auth;

import static play.mvc.Results.redirect;

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
    if (code == HttpConstants.FORBIDDEN) {
      return redirect(controllers.routes.AuthorisationController.unauthorised());
    } else {
      return super.adapt(code, context);
    }
  }

}
