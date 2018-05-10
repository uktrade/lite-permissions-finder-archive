package controllers;

import com.google.inject.Inject;
import exceptions.BusinessRuleException;
import models.enums.Action;
import models.enums.PageType;
import models.view.AnswerView;
import models.view.CheckboxView;
import models.view.form.AnswerForm;
import models.view.form.MultiAnswerForm;
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
import views.html.triage.decontrol;
import views.html.triage.decontrolOutcome;
import views.html.triage.dropout;
import views.html.triage.selectMany;
import views.html.triage.selectOne;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@With(SessionGuardAction.class)
public class StageController extends Controller {

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
  public StageController(SessionService sessionService, FormFactory formFactory,
                         JourneyConfigService journeyConfigService, HtmlRenderService htmlRenderService,
                         selectOne selectOne, selectMany selectMany, dropout dropout, decontrol decontrol,
                         decontrolOutcome decontrolOutcome) {
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

  public Result outcomeDecontrol(String sessionId) {
    return ok(decontrolOutcome.render(sessionId));
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
      String initialStageId = journeyConfigService.getInitialStageId();
      return redirect(routes.StageController.render(initialStageId, sessionId));
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

  private Result renderSelectOne(Form<AnswerForm> answerFormForm, String stageId, String sessionId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    String title = stageConfig.getQuestionTitle().orElse("Select one");
    return ok(selectOne.render(answerFormForm, stageId, sessionId, title, createAnswerViews(stageConfig)));
  }

  private Result renderDecontrol(Form<MultiAnswerForm> multiAnswerForm, String stageId, String sessionId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    List<AnswerView> answerViews = createAnswerViews(stageConfig);
    List<CheckboxView> checkboxViews = answerViews.stream()
        .map(answerView -> new CheckboxView(answerView, !multiAnswerForm.hasErrors() && multiAnswerForm.get().answers.contains(answerView.getValue())))
        .collect(Collectors.toList());
    String controlCode = stageConfig.getRelatedControlEntry()
        .orElseThrow(() -> new BusinessRuleException("Missing relatedControlEntry for decontrol stage " + stageId))
        .getControlCode();
    return ok(decontrol.render(multiAnswerForm, stageId, sessionId, controlCode, checkboxViews));
  }

  private Result renderSelectMany(Form<MultiAnswerForm> multiAnswerFormForm, String stageId, String sessionId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    String title = stageConfig.getQuestionTitle().orElse("Select at least one");
    List<AnswerView> answerViews = createAnswerViews(stageConfig);
    List<CheckboxView> checkboxViews = answerViews.stream()
        .map(answerView -> new CheckboxView(answerView, false))
        .collect(Collectors.toList());
    return ok(selectMany.render(multiAnswerFormForm, stageId, sessionId, title, checkboxViews));
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
        List<AnswerConfig> matchingAnswers = getMatchingAnswers(actualAnswers, stageConfig);
        if (matchingAnswers.isEmpty()) {
          return renderDecontrol(multiAnswerFormForm.withError("answers", "Please select at least one answer"),
              stageId, sessionId);
        } else {
          Set<String> answerIds = matchingAnswers.stream().map(AnswerConfig::getAnswerId).collect(Collectors.toSet());
          sessionService.saveAnswersForStageId(sessionId, stageId, answerIds);
          return redirect(routes.StageController.outcomeDecontrol(sessionId));
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
        List<AnswerConfig> matchingAnswers = getMatchingAnswers(actualAnswers, stageConfig);
        if (matchingAnswers.isEmpty()) {
          return renderSelectMany(multiAnswerFormForm.withError("answers", "Please select at least one answer"),
              stageId, sessionId);
        } else {
          AnswerConfig answerConfig = getAnswerConfigWithLowestPrecedence(matchingAnswers);
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

  private List<AnswerConfig> getMatchingAnswers(List<String> actualAnswers, StageConfig stageConfig) {
    return stageConfig.getAnswerConfigs()
        .stream()
        .filter(answerConfig -> actualAnswers.contains(answerConfig.getAnswerId()))
        .collect(Collectors.toList());
  }

  private AnswerConfig getAnswerConfigWithLowestPrecedence(List<AnswerConfig> answerConfigs) {
    return answerConfigs.stream()
        .sorted(Comparator.comparing(AnswerConfig::getAnswerPrecedence))
        .findFirst()
        .orElse(null);
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

  private List<AnswerView> createAnswerViews(StageConfig stageConfig) {
    return stageConfig.getAnswerConfigs().stream()
        .sorted(Comparator.comparing(AnswerConfig::getAnswerPrecedence))
        .map(this::createAnswerView)
        .collect(Collectors.toList());
  }

  private AnswerView createAnswerView(AnswerConfig answerConfig) {
    Optional<ControlEntryConfig> associatedControlEntryConfig = answerConfig.getAssociatedControlEntryConfig();
    if (associatedControlEntryConfig.isPresent()) {
      ControlEntryConfig controlEntryConfig = associatedControlEntryConfig.get();
      return new AnswerView(htmlRenderService.convertRichTextToPlainText(controlEntryConfig.getFullDescription()), answerConfig.getAnswerId());
    } else {
      Optional<RichText> labelText = answerConfig.getLabelText();
      if (labelText.isPresent()) {
        return new AnswerView(htmlRenderService.convertRichTextToPlainText(labelText.get()), answerConfig.getAnswerId());
      } else {
        throw new BusinessRuleException("Both answerConfig.getAssociatedControlEntryConfig and answerConfig.getLabelText are absent.");
      }
    }
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
