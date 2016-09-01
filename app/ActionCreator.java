import com.google.inject.Inject;
import components.common.CommonContextActionFactory;
import play.mvc.Action;
import play.mvc.Http;

import java.lang.reflect.Method;

public class ActionCreator implements play.http.ActionCreator {

  private final CommonContextActionFactory commonContextActionFactory;

  @Inject
  public ActionCreator(CommonContextActionFactory commonContextActionFactory) {
    this.commonContextActionFactory = commonContextActionFactory;
  }

  @Override
  public Action createAction(Http.Request request, Method actionMethod) {
    return commonContextActionFactory.createAction(request);
  }
}
