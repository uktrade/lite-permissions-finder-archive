package controllers;

import com.google.inject.Inject;
import components.services.AnswerViewService;
import components.services.BreadcrumbViewService;
import components.services.RenderService;
import models.enums.PageType;
import models.view.AnswerView;
import models.view.BreadcrumbView;
import models.view.form.RequestNlrForm;
import models.view.form.RequestOgelForm;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.session.SessionService;
import utils.PageTypeUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OutcomeController extends Controller {

  private final JourneyConfigService journeyConfigService;
  private final SessionService sessionService;
  private final FormFactory formFactory;
  private final AnswerViewService answerViewService;
  private final BreadcrumbViewService breadcrumbViewService;
  private final RenderService renderService;
  private final views.html.triage.dropout dropout;
  private final views.html.triage.decontrolOutcome decontrolOutcome;
  private final views.html.triage.listedOutcome listedOutcome;

  @Inject
  public OutcomeController(JourneyConfigService journeyConfigService, SessionService sessionService,
                           FormFactory formFactory, AnswerViewService answerViewService,
                           BreadcrumbViewService breadcrumbViewService, RenderService renderService,
                           views.html.triage.dropout dropout, views.html.triage.decontrolOutcome decontrolOutcome,
                           views.html.triage.listedOutcome listedOutcome) {
    this.journeyConfigService = journeyConfigService;
    this.sessionService = sessionService;
    this.formFactory = formFactory;
    this.answerViewService = answerViewService;
    this.breadcrumbViewService = breadcrumbViewService;
    this.renderService = renderService;
    this.dropout = dropout;
    this.decontrolOutcome = decontrolOutcome;
    this.listedOutcome = listedOutcome;
  }

  public Result outcomeListed(String controlEntryId, String sessionId) {
    Form<RequestOgelForm> form = formFactory.form(RequestOgelForm.class);
    return renderOutcomeListed(form, journeyConfigService.getControlEntryConfigForId(controlEntryId), sessionId);
  }

  public Result handleOutcomeListedSubmit(String controlEntryId, String sessionId) {
    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigForId(controlEntryId);
    //TODO graceful handling if control entry not found
    Form<RequestOgelForm> form = formFactory.form(RequestOgelForm.class).bindFromRequest();
    if (form.hasErrors() || !"true".equals(form.rawData().get("answer"))) {
      return renderOutcomeListed(form, controlEntryConfig, sessionId);
    } else {
      return redirect(controllers.licencefinder.routes.TestEntryController.testEntry(controlEntryConfig.getControlCode()));
    }
  }

  public Result outcomeDecontrol(String stageId, String sessionId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    if (stageConfig == null || PageTypeUtil.getPageType(stageConfig) != PageType.DECONTROL) {
      return redirectToIndex(sessionId);
    } else {
      Set<String> answers = sessionService.getAnswersForStageId(sessionId, stageId);
      if (answers.isEmpty()) {
        Logger.error("Answers cannot be empty on outcome decontrol page.");
        return redirectToIndex(sessionId);
      } else {
        Form<RequestNlrForm> form = formFactory.form(RequestNlrForm.class);
        return renderOutcomeDecontrol(form, stageId, sessionId, answers);
      }
    }
  }

  public Result handleOutcomeDecontrolSubmit(String stageId, String sessionId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    if (stageConfig == null || PageTypeUtil.getPageType(stageConfig) != PageType.DECONTROL) {
      return redirectToIndex(sessionId);
    } else {
      Set<String> answers = sessionService.getAnswersForStageId(sessionId, stageId);
      if (answers.isEmpty()) {
        Logger.error("Answers cannot be empty on outcome decontrol page.");
        return redirectToIndex(sessionId);
      } else {
        Form<RequestNlrForm> form = formFactory.form(RequestNlrForm.class).bindFromRequest();
        if (form.hasErrors() || !"true".equals(form.rawData().get("answer"))) {
          return renderOutcomeDecontrol(form, stageId, sessionId, answers);
        } else {
          return ok("TODO: Login, show form");
        }
      }
    }
  }

  public Result outcomeDropout(String sessionId) {
    return ok(dropout.render(sessionId));
  }

  private Result renderOutcomeListed(Form<RequestOgelForm> requestOgelForm, ControlEntryConfig controlEntryConfig, String sessionId) {
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(controlEntryConfig);
    String controlCode = controlEntryConfig.getControlCode();
    String description = renderService.getFullDescription(controlEntryConfig);
    return ok(listedOutcome.render(requestOgelForm, controlEntryConfig.getId(), sessionId, breadcrumbView, controlCode, description));
  }

  private Result renderOutcomeDecontrol(Form<RequestNlrForm> requestNlrForm, String stageId, String sessionId,
                                        Set<String> answers) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    List<String> controlCodes = answerViewService.createAnswerViews(stageConfig).stream()
        .filter(answer -> answers.contains(answer.getValue()))
        .map(AnswerView::getPrompt)
        .collect(Collectors.toList());
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageId);
    return ok(decontrolOutcome.render(requestNlrForm, stageId, sessionId, breadcrumbView, controlCodes));
  }

  private Result redirectToIndex(String sessionId) {
    String initialStageId = journeyConfigService.getInitialStageId();
    return redirect(routes.StageController.render(initialStageId, sessionId));
  }

}
