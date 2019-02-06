package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import models.callback.ControlEntryResponse;
import org.apache.commons.lang.StringUtils;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import triage.session.SessionService;
import triage.session.TriageSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
@Slf4j
public class JumpToController extends Controller {

  // TODO Remove eventually
  private final ControlEntryDao controlEntryDao;
  private final SessionService sessionService;

  private final views.html.jumpTo jumpTo;

  public Result finder(String query) {
    // If value is blank, return empty list
    if (StringUtils.isBlank(query)) {
      return ok(jumpTo.render(query, new ArrayList<>()));
    }

    // Filter database and map to response class
    List<ControlEntryResponse> controlEntries = controlEntryDao.findControlEntriesByControlCode(query)
      .stream()
      .map(ControlEntryResponse::new)
      .collect(Collectors.toList());

    return ok(jumpTo.render(query, controlEntries));
  }

  public Result jump(String stageId) {
    TriageSession triageSession = sessionService.createNewSession();
    return redirect(routes.StageController.render(stageId, triageSession.getId()));
  }

  public Result controlCodeSearch(String value) {
    List<ControlEntryResponse> controlEntries = controlEntryDao.findControlEntriesByControlCode(value)
      .stream()
      .map(ControlEntryResponse::new)
      .collect(Collectors.toList());
    return ok(Json.prettyPrint(new ObjectMapper().valueToTree(controlEntries)));
  }

}
