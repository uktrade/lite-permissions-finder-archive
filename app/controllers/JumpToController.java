package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import components.services.ControlEntryService;
import lombok.AllArgsConstructor;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import triage.session.SessionService;
import triage.session.TriageSession;

@AllArgsConstructor(onConstructor = @__({@Inject}))
public class JumpToController extends Controller {

  private final ControlEntryService controlEntryService;
  private final SessionService sessionService;

  private final views.html.jumpTo jumpTo;

  public Result finder(String value) {
    return ok(jumpTo.render(value, controlEntryService.findControlEntriesByControlCode(value)));
  }

  public Result jump(String stageId) {
    TriageSession triageSession = sessionService.createNewSession();
    return redirect(routes.StageController.render(stageId, triageSession.getId()));
  }

  public Result controlCodeSearch(String value) {
    return ok(Json.prettyPrint(new ObjectMapper()
      .valueToTree(controlEntryService.findControlEntriesByControlCode(value))));
  }
}
