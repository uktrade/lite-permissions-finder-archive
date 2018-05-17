package controllers;

import com.google.inject.Inject;
import components.services.AnswerConfigService;
import components.services.AnswerViewService;
import components.services.BreadcrumbViewService;
import exceptions.BusinessRuleException;
import models.enums.Action;
import models.enums.PageType;
import models.view.AnswerView;
import models.view.BreadcrumbView;
import models.view.CheckboxView;
import models.view.form.AnswerForm;
import models.view.form.MultiAnswerForm;
import models.view.form.RequestNlrForm;
import org.apache.commons.collections4.ListUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import triage.config.AnswerConfig;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.session.SessionService;
import triage.text.HtmlRenderService;
import triage.text.RichText;
import utils.EnumUtil;
import utils.common.SelectOption;
import views.html.triage.decontrol;
import views.html.triage.decontrolOutcome;
import views.html.triage.dropout;
import views.html.triage.selectMany;
import views.html.triage.selectOne;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@With(SessionGuardAction.class)
public class StageController extends Controller {

  private final BreadcrumbViewService breadcrumbViewService;
  private final AnswerConfigService answerConfigService;
  private final AnswerViewService answerViewService;
  private final SessionService sessionService;
  private final FormFactory formFactory;
  private final JourneyConfigService journeyConfigService;
  private final HtmlRenderService htmlRenderService;
  private final decontrol decontrol;
  private final selectOne selectOne;
  private final selectMany selectMany;
  private final dropout dropout;
  private final decontrolOutcome decontrolOutcome;

  @Inject
  public StageController(BreadcrumbViewService breadcrumbViewService,
                         AnswerConfigService answerConfigService,
                         AnswerViewService answerViewService, SessionService sessionService,
                         FormFactory formFactory,
                         JourneyConfigService journeyConfigService, HtmlRenderService htmlRenderService,
                         selectOne selectOne, selectMany selectMany, dropout dropout, decontrol decontrol,
                         decontrolOutcome decontrolOutcome) {
    this.breadcrumbViewService = breadcrumbViewService;
    this.answerConfigService = answerConfigService;
    this.answerViewService = answerViewService;
    this.sessionService = sessionService;
    this.formFactory = formFactory;
    this.journeyConfigService = journeyConfigService;
    this.htmlRenderService = htmlRenderService;
    this.selectOne = selectOne;
    this.selectMany = selectMany;
    this.dropout = dropout;
    this.decontrol = decontrol;
    this.decontrolOutcome = decontrolOutcome;
  }

  public Result outcomeDecontrol(String stageId, String sessionId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    if (stageConfig == null || getPageType(stageConfig) != PageType.DECONTROL) {
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
    if (stageConfig == null || getPageType(stageConfig) != PageType.DECONTROL) {
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

  public Result index(String sessionId) {
    return redirectToIndex(sessionId);
  }

  public Result render(String stageId, String sessionId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    if (stageConfig == null) {
      return redirectToIndex(sessionId);
    } else {
      PageType pageType = getPageType(stageConfig);
      switch (pageType) {
        case SELECT_ONE:
          AnswerForm answerForm = new AnswerForm();
          answerForm.answer = sessionService.getAnswersForStageId(sessionId, stageId).stream().findFirst().orElse(null);
          Form<AnswerForm> filledAnswerForm = formFactory.form(AnswerForm.class).fill(answerForm);
          return renderSelectOne(filledAnswerForm, stageId, sessionId);
        case SELECT_MANY:
          MultiAnswerForm multiAnswerForm = new MultiAnswerForm();
          multiAnswerForm.answers = new ArrayList<>(sessionService.getAnswersForStageId(sessionId, stageId));
          Form<MultiAnswerForm> filledMultiAnswerFormForm = formFactory.form(MultiAnswerForm.class).fill(multiAnswerForm);
          return renderSelectMany(filledMultiAnswerFormForm, stageId, sessionId);
        case DECONTROL:
          MultiAnswerForm form = new MultiAnswerForm();
          form.answers = new ArrayList<>(sessionService.getAnswersForStageId(sessionId, stageId));
          Form<MultiAnswerForm> filledForm = formFactory.form(MultiAnswerForm.class).fill(form);
          return renderDecontrol(filledForm, stageId, sessionId);
        case UNKNOWN:
        default:
          Logger.error("Unknown stageId " + stageId);
          return redirectToIndex(sessionId);
      }
    }
  }

  public Result handleSubmit(String stageId, String sessionId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    if (stageConfig == null) {
      Logger.error("Unknown stageId " + stageId);
      return redirectToIndex(sessionId);
    } else {
      PageType pageType = getPageType(stageConfig);
      switch (pageType) {
        case SELECT_ONE:
          return handleSelectOneSubmit(stageId, sessionId, stageConfig);
        case SELECT_MANY:
          return handleSelectManySubmit(stageId, sessionId, stageConfig);
        case DECONTROL:
          return handleDecontrolSubmit(stageId, sessionId, stageConfig);
        case UNKNOWN:
        default:
          Logger.error("Unknown stageId " + stageId);
          return redirectToIndex(sessionId);
      }
    }
  }

  private Result renderOutcomeDecontrol(Form<RequestNlrForm> requestNlrFormForm, String stageId, String sessionId,
                                        Set<String> answers) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    List<String> controlCodes = answerViewService.createAnswerViews(stageConfig).stream()
        .filter(answer -> answers.contains(answer.getValue()))
        .map(AnswerView::getPrompt)
        .collect(Collectors.toList());
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageId);
    return ok(decontrolOutcome.render(requestNlrFormForm, stageId, sessionId, breadcrumbView, controlCodes));
  }

  private Result renderSelectOne(Form<AnswerForm> answerFormForm, String stageId, String sessionId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    String title = stageConfig.getQuestionTitle().orElse("Select one");
    String explanatoryText = getExplanatoryText(stageConfig);
    List<AnswerView> answerViews = answerViewService.createAnswerViews(stageConfig);
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageId);
    return ok(selectOne.render(answerFormForm, stageId, sessionId, title, explanatoryText, answerViews, breadcrumbView));
  }

  private String getExplanatoryText(StageConfig stageConfig) {
    Optional<RichText> explanatoryNoteOptional = stageConfig.getExplanatoryNote();
    if (explanatoryNoteOptional.isPresent()) {
      RichText explanatoryNote = explanatoryNoteOptional.get();
      return htmlRenderService.convertRichTextToPlainText(explanatoryNote);
    } else {
      return null;
    }
  }

  private Result renderDecontrol(Form<MultiAnswerForm> multiAnswerForm, String stageId, String sessionId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    List<AnswerView> answerViews = answerViewService.createAnswerViews(stageConfig);
    ControlEntryConfig controlEntryConfig = stageConfig.getRelatedControlEntry()
        .orElseThrow(() -> new BusinessRuleException("Missing relatedControlEntry for decontrol stage " + stageId));
    String controlCode = controlEntryConfig.getControlCode();
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageId);
    return ok(decontrol.render(multiAnswerForm, stageId, sessionId, controlCode, answerViews, breadcrumbView));
  }

  private Result renderSelectMany(Form<MultiAnswerForm> multiAnswerFormForm, String stageId, String sessionId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    String title = stageConfig.getQuestionTitle().orElse("Select at least one");
    String explanatoryText = getExplanatoryText(stageConfig);
    List<AnswerView> answerViews = answerViewService.createAnswerViews(stageConfig);
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageId);
    return ok(selectMany.render(multiAnswerFormForm, stageId, sessionId, title, explanatoryText, answerViews, breadcrumbView));
  }

  private Result handleDecontrolSubmit(String stageId, String sessionId, StageConfig stageConfig) {
    Form<MultiAnswerForm> multiAnswerFormForm = formFactory.form(MultiAnswerForm.class).bindFromRequest();
    String actionParam = multiAnswerFormForm.rawData().get("action");
    Action action = EnumUtil.parse(actionParam, Action.class);
    if (multiAnswerFormForm.hasErrors()) {
      Logger.error("MultiAnswerForm has unexpected errors");
      return redirectToStage(stageId, sessionId);
    } else {
      if (action == Action.CONTINUE) {
        List<String> actualAnswers = ListUtils.emptyIfNull(multiAnswerFormForm.get().answers);
        List<AnswerConfig> matchingAnswers = answerConfigService.getMatchingAnswerConfigs(actualAnswers, stageConfig);
        if (matchingAnswers.isEmpty()) {
          return renderDecontrol(multiAnswerFormForm.withError("answers", "Please select at least one answer"),
              stageId, sessionId);
        } else {
          Set<String> answerIds = matchingAnswers.stream().map(AnswerConfig::getAnswerId).collect(Collectors.toSet());
          sessionService.saveAnswersForStageId(sessionId, stageId, answerIds);
          return redirect(routes.StageController.outcomeDecontrol(stageId, sessionId));
        }
      } else if (action == Action.NONE) {
        Optional<String> nextStageId = stageConfig.getNextStageId();
        if (nextStageId.isPresent()) {
          return redirectToStage(nextStageId.get(), sessionId);
        } else {
          Logger.error("Decontrol stageConfig doesn't have nextStageId");
          return redirectToStage(stageId, sessionId);
        }
      } else {
        Logger.error("Unknown action " + actionParam);
        return redirectToStage(stageId, sessionId);
      }
    }
  }

  private Result handleSelectManySubmit(String stageId, String sessionId, StageConfig stageConfig) {
    Form<MultiAnswerForm> multiAnswerFormForm = formFactory.form(MultiAnswerForm.class).bindFromRequest();
    String actionParam = multiAnswerFormForm.rawData().get("action");
    Action action = EnumUtil.parse(actionParam, Action.class);
    if (multiAnswerFormForm.hasErrors()) {
      Logger.error("MultiAnswerForm has unexpected errors");
      return redirectToStage(stageId, sessionId);
    } else {
      if (action == Action.CONTINUE) {
        List<String> actualAnswers = ListUtils.emptyIfNull(multiAnswerFormForm.get().answers);
        List<AnswerConfig> matchingAnswers = answerConfigService.getMatchingAnswerConfigs(actualAnswers, stageConfig);
        if (matchingAnswers.isEmpty()) {
          return renderSelectMany(multiAnswerFormForm.withError("answers", "Please select at least one answer"),
              stageId, sessionId);
        } else {
          AnswerConfig answerConfig = answerConfigService.getAnswerConfigWithLowestPrecedence(matchingAnswers);
          Optional<String> nextStageId = answerConfig.getNextStageId();
          if (nextStageId.isPresent()) {
            Set<String> answerIds = matchingAnswers.stream().map(AnswerConfig::getAnswerId).collect(Collectors.toSet());
            sessionService.saveAnswersForStageId(sessionId, stageId, answerIds);
            return redirectToStage(nextStageId.get(), sessionId);
          } else {
            Logger.error("AnswerConfig doesn't have next stageId.");
            return redirectToStage(stageId, sessionId);
          }
        }
      } else if (action == Action.NONE) {
        return redirect(routes.StageController.outcomeDropout(sessionId));
      } else {
        Logger.error("Unknown action " + actionParam);
        return redirectToStage(stageId, sessionId);
      }
    }
  }

  private Result handleSelectOneSubmit(String stageId, String sessionId, StageConfig stageConfig) {
    Form<AnswerForm> answerForm = formFactory.form(AnswerForm.class).bindFromRequest();
    String actionParam = answerForm.rawData().get("action");
    String answer = answerForm.rawData().get("answer");
    Action action = EnumUtil.parse(actionParam, Action.class);
    if (action == Action.CONTINUE) {
      Optional<AnswerConfig> answerConfigOptional = stageConfig.getAnswerConfigs().stream()
          .filter(answerConfigIterate -> answerConfigIterate.getAnswerId().equals(answer))
          .findAny();
      if (answerConfigOptional.isPresent()) {
        AnswerConfig answerConfig = answerConfigOptional.get();
        Optional<String> nextStageId = answerConfig.getNextStageId();
        if (nextStageId.isPresent()) {
          sessionService.saveAnswersForStageId(sessionId, stageId, Collections.singleton(answerConfig.getAnswerId()));
          return redirectToStage(nextStageId.get(), sessionId);
        } else {
          Logger.error("answerConfig.get.getNextStageId is absent.");
          return redirectToStage(stageId, sessionId);
        }
      } else {
        Logger.error("Unknown answer " + answer);
        return renderSelectOne(answerForm, stageId, sessionId);
      }
    } else if (action == Action.NONE) {
      return redirect(routes.StageController.outcomeDropout(sessionId));
    } else {
      Logger.error("Unknown action " + actionParam);
      return redirectToStage(stageId, sessionId);
    }
  }

  private Result redirectToIndex(String sessionId) {
    String initialStageId = journeyConfigService.getInitialStageId();
    return redirect(routes.StageController.render(initialStageId, sessionId));
  }

  private Result redirectToStage(String stageId, String sessionId) {
    return redirect(routes.StageController.render(stageId, sessionId));
  }

  private PageType getPageType(StageConfig stageConfig) {
    if (stageConfig.getQuestionType() == StageConfig.QuestionType.STANDARD && stageConfig.getAnswerType() == StageConfig.AnswerType.SELECT_ONE) {
      return PageType.SELECT_ONE;
    } else if (stageConfig.getQuestionType() == StageConfig.QuestionType.STANDARD && stageConfig.getAnswerType() == StageConfig.AnswerType.SELECT_MANY) {
      return PageType.SELECT_MANY;
    } else if (stageConfig.getQuestionType() == StageConfig.QuestionType.DECONTROL && stageConfig.getAnswerType() == StageConfig.AnswerType.SELECT_MANY) {
      return PageType.DECONTROL;
    } else {
      return PageType.UNKNOWN;
    }
  }

}
