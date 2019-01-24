package controllers;

import com.google.inject.Inject;
import components.services.AnswerViewService;
import components.services.BreadcrumbViewService;
import components.services.ProgressViewService;
import components.services.RenderService;
import controllers.guard.SessionGuardAction;
import exceptions.UnknownParameterException;
import lombok.AllArgsConstructor;
import models.enums.PageType;
import models.view.AnswerView;
import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import models.view.ProgressView;
import models.view.RelatedEntryView;
import models.view.SubAnswerView;
import models.view.form.RequestNlrForm;
import models.view.form.RequestOgelForm;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import triage.config.ControlEntryConfig;
import triage.config.ControllerConfigService;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.session.SessionService;
import utils.PageTypeUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@With(SessionGuardAction.class)
@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class OutcomeController extends Controller {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OutcomeController.class);

  private final JourneyConfigService journeyConfigService;
  private final ControllerConfigService controllerConfigService;
  private final SessionService sessionService;
  private final FormFactory formFactory;
  private final AnswerViewService answerViewService;
  private final BreadcrumbViewService breadcrumbViewService;
  private final RenderService renderService;
  private final ProgressViewService progressViewService;
  private final views.html.triage.dropout dropout;
  private final views.html.triage.decontrolOutcome decontrolOutcome;
  private final views.html.triage.listedOutcome listedOutcome;
  private final views.html.triage.itemNotFound itemNotFound;
  private final views.html.triage.noResult noResult;

  public Result outcomeNoResult(String controlEntryId, String sessionId) {
    ControlEntryConfig controlEntryConfig = controllerConfigService.getControlEntryConfig(controlEntryId);

    ProgressView progressView = progressViewService.createProgressView(controlEntryConfig);
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbViewFromControlEntry(sessionId, controlEntryConfig);
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    return ok(noResult.render(sessionId, resumeCode, progressView, breadcrumbView));
  }

  public Result outcomeItemNotFound(String controlEntryId, String sessionId) {
    ControlEntryConfig controlEntryConfig = controllerConfigService.getControlEntryConfig(controlEntryId);

    Form<RequestNlrForm> requestNlrFormForm = formFactory.form(RequestNlrForm.class);
    return renderItemNotFound(requestNlrFormForm, controlEntryConfig, sessionId);
  }

  public Result handleOutcomeItemNotFoundSubmit(String controlEntryId, String sessionId) {
    ControlEntryConfig controlEntryConfig = controllerConfigService.getControlEntryConfig(controlEntryId);

    Form<RequestNlrForm> form = formFactory.form(RequestNlrForm.class).bindFromRequest();
    if (form.hasErrors() || !isChecked(form)) {
      return renderItemNotFound(form, controlEntryConfig, sessionId);
    } else {
      return redirect(routes.ViewOutcomeController.registerNotFoundNlr(sessionId, controlEntryId));
    }
  }

  public Result outcomeListed(String controlEntryId, String sessionId) {
    ControlEntryConfig controlEntryConfig = controllerConfigService.getControlEntryConfig(controlEntryId);

    Form<RequestOgelForm> form = formFactory.form(RequestOgelForm.class);
    return renderOutcomeListed(form, controlEntryConfig, sessionId);
  }

  public Result handleOutcomeListedSubmit(String controlEntryId, String sessionId) {
    ControlEntryConfig controlEntryConfig = controllerConfigService.getControlEntryConfig(controlEntryId);

    Form<RequestOgelForm> form = formFactory.form(RequestOgelForm.class).bindFromRequest();
    if (form.hasErrors() || !isChecked(form)) {
      return renderOutcomeListed(form, controlEntryConfig, sessionId);
    } else {
      return redirect(routes.ViewOutcomeController.saveListedOutcome(sessionId, controlEntryId));
    }
  }

  public Result outcomeDecontrol(String stageId, String sessionId) {
    StageConfig stageConfig = controllerConfigService.getStageConfig(stageId);

    Set<String> answers = sessionService.getAnswerIdsForStageId(sessionId, stageId);
   /* if (answers.isEmpty()) {
      LOGGER.error("Answers cannot be empty on outcome decontrol page.");
      return redirectToIndex(sessionId);
    } else {*/
      Form<RequestNlrForm> form = formFactory.form(RequestNlrForm.class);
      return renderOutcomeDecontrol(form, stageConfig, sessionId, answers);
    //}
  }

  public Result outcomeDecontrol2(String stageId, String sessionId) {
    StageConfig stageConfig = controllerConfigService.getStageConfig(stageId);

    if (PageTypeUtil.getPageType(stageConfig) != PageType.DECONTROL) {
      throw UnknownParameterException.unknownStageId(stageId);
    } else {
      Set<String> answers = sessionService.getAnswerIdsForStageId(sessionId, stageId);
      if (answers.isEmpty()) {
        LOGGER.error("Answers cannot be empty on outcome decontrol page.");
        return redirectToIndex(sessionId);
      } else {
        Form<RequestNlrForm> form = formFactory.form(RequestNlrForm.class);
        return renderOutcomeDecontrol(form, stageConfig, sessionId, answers);
      }
    }
  }

  public Result handleOutcomeDecontrolSubmit(String stageId, String sessionId) {
    StageConfig stageConfig = controllerConfigService.getStageConfig(stageId);

    if (PageTypeUtil.getPageType(stageConfig) != PageType.DECONTROL) {
      throw UnknownParameterException.unknownStageId(stageId);
    } else {
      Set<String> answers = sessionService.getAnswerIdsForStageId(sessionId, stageConfig.getStageId());
      if (answers.isEmpty()) {
        LOGGER.error("Answers cannot be empty on outcome decontrol page.");
        return redirectToIndex(sessionId);
      } else {
        Form<RequestNlrForm> form = formFactory.form(RequestNlrForm.class).bindFromRequest();
        if (form.hasErrors() || !isChecked(form)) {
          return renderOutcomeDecontrol(form, stageConfig, sessionId, answers);
        } else {
          return redirect(routes.ViewOutcomeController.registerDecontrolNlr(sessionId, stageConfig.getStageId()));
        }
      }
    }
  }

  public Result outcomeDropout(String sessionId) {
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    return ok(dropout.render(sessionId, resumeCode));
  }

  private Result renderItemNotFound(Form<RequestNlrForm> requestNlrFormForm, ControlEntryConfig controlEntryConfig,
                                    String sessionId) {
    String changeUrl = journeyConfigService.getPrincipleStageConfigForControlEntry(controlEntryConfig)
        .map(stageConfig -> routes.StageController.render(stageConfig.getStageId(), sessionId).toString())
        .orElseGet(() -> {
          LOGGER.warn("Unable to create changeUrl for controlEntryId " + controlEntryConfig.getId());
          return null;
        });

    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(sessionId,
        controlEntryConfig, true);
    List<RelatedEntryView> relatedEntryViews = breadcrumbViewService.createRelatedEntryViews(sessionId,
        controlEntryConfig, true);

    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    return ok(itemNotFound.render(requestNlrFormForm, controlEntryConfig.getId(), sessionId, resumeCode,
        breadcrumbItemViews, relatedEntryViews, changeUrl));
  }

  private Result renderOutcomeListed(Form<RequestOgelForm> requestOgelForm, ControlEntryConfig controlEntryConfig,
                                     String sessionId) {
    List<BreadcrumbItemView> breadcrumbViews = breadcrumbViewService.createBreadcrumbItemViews(sessionId, controlEntryConfig, true);
    String controlCode = controlEntryConfig.getControlCode();
    String description = renderService.getFullDescription(controlEntryConfig);
    List<SubAnswerView> subAnswerViews = answerViewService.createSubAnswerViews(controlEntryConfig, true);
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    return ok(listedOutcome.render(requestOgelForm, controlEntryConfig.getId(), sessionId, resumeCode, breadcrumbViews, controlCode, description, subAnswerViews));
  }

  private Result renderOutcomeDecontrol(Form<RequestNlrForm> requestNlrForm, StageConfig stageConfig, String sessionId,
                                        Set<String> answers) {
    List<AnswerView> answerViews = answerViewService.createAnswerViews(stageConfig, true).stream()
        .filter(answer -> answers.contains(answer.getValue()))
        .collect(Collectors.toList());
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageConfig, sessionId, true);
    String decontrolUrl = breadcrumbViewService.createDecontrolUrl(sessionId, breadcrumbViewService.getControlEntryConfig(stageConfig));
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    return ok(decontrolOutcome.render(requestNlrForm, stageConfig.getStageId(), sessionId, resumeCode, decontrolUrl, breadcrumbView, answerViews));
  }

  private Result redirectToIndex(String sessionId) {
    String initialStageId = journeyConfigService.getInitialStageId();
    return redirect(routes.StageController.render(initialStageId, sessionId));
  }

  private boolean isChecked(Form form) {
    return "true".equals(form.rawData().get("answer"));
  }

}
