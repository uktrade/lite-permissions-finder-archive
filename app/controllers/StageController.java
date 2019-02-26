package controllers;

import com.google.inject.Inject;
import components.services.AnswerConfigService;
import components.services.AnswerViewService;
import components.services.BreadcrumbViewService;
import components.services.JourneyService;
import components.services.ProgressViewService;
import components.services.RenderService;
import controllers.guard.StageGuardAction;
import exceptions.BusinessRuleException;
import exceptions.UnknownParameterException;
import lombok.AllArgsConstructor;
import models.cms.Journey;
import models.cms.enums.OutcomeType;
import models.enums.Action;
import models.enums.PageType;
import models.view.AnswerView;
import models.view.BreadcrumbView;
import models.view.ProgressView;
import models.view.SubAnswerView;
import models.view.form.AnswerForm;
import models.view.form.MultiAnswerForm;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import triage.config.AnswerConfig;
import triage.config.ControlEntryConfig;
import triage.config.ControllerConfigService;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.session.SessionService;
import utils.EnumUtil;
import utils.PageTypeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@With(StageGuardAction.class)
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class StageController extends Controller {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StageController.class);

  private static final String ACTION = "action";

  private final BreadcrumbViewService breadcrumbViewService;
  private final AnswerConfigService answerConfigService;
  private final AnswerViewService answerViewService;
  private final SessionService sessionService;
  private final FormFactory formFactory;
  private final JourneyConfigService journeyConfigService;
  private final ControllerConfigService controllerConfigService;
  private final RenderService renderService;
  private final JourneyService journeyService;
  private final ProgressViewService progressViewService;
  private final views.html.triage.decontrol decontrol;
  private final views.html.triage.decontrolFurtherChecks decontrolFurtherChecks;
  private final views.html.triage.selectOne selectOne;
  private final views.html.triage.selectMany selectMany;
  private final views.html.triage.relatedEntries relatedEntries;
  private final views.html.triage.item item;

  public Result index(String sessionId) {
    return redirectToIndex(sessionId);
  }

  public Result render(String stageId, String sessionId) {
    StageConfig stageConfig = controllerConfigService.getStageConfig(stageId);

    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    PageType pageType = PageTypeUtil.getPageType(stageConfig);
    switch (pageType) {
      case SELECT_ONE:
        AnswerForm answerForm = new AnswerForm();
        answerForm.answer = sessionService.getAnswerIdsForStageId(sessionId, stageId).stream().findFirst().orElse(null);
        Form<AnswerForm> filledAnswerForm = formFactory.form(AnswerForm.class).fill(answerForm);
        return renderSelectOne(filledAnswerForm, stageConfig, sessionId, resumeCode);
      case SELECT_MANY:
        MultiAnswerForm multiAnswerForm = new MultiAnswerForm();
        multiAnswerForm.setAnswers(new ArrayList<>(sessionService.getAnswerIdsForStageId(sessionId, stageId)));
        Form<MultiAnswerForm> filledMultiAnswerFormForm = formFactory.form(MultiAnswerForm.class).fill(multiAnswerForm);
        return renderSelectMany(filledMultiAnswerFormForm, stageConfig, sessionId, resumeCode);
      case DECONTROL:
        MultiAnswerForm form = new MultiAnswerForm();
        form.setAnswers(new ArrayList<>(sessionService.getAnswerIdsForStageId(sessionId, stageId)));
        Form<MultiAnswerForm> filledForm = formFactory.form(MultiAnswerForm.class).fill(form);
        return renderDecontrol(filledForm, stageConfig, sessionId, resumeCode);
      case ITEM:
        return renderItem(formFactory.form(AnswerForm.class), stageConfig, sessionId, resumeCode);
      case FURTHER_DECONTROL_CHECKS:
        return makeFurtherDecontrolChecksResult(sessionId, stageConfig);
      case UNKNOWN:
      default:
        throw UnknownParameterException.unknownStageId(stageId);
    }
  }

  private Result makeFurtherDecontrolChecksResult(String sessionId, StageConfig stageConfig) {
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    String currentListName = journeyService.getById(stageConfig.getJourneyId()).getFriendlyJourneyName();
    Set<String> listsToCheck = stageConfig.getRelatedControlEntry().get().getJumpToControlEntryIds()
      .stream()
      .map(controllerConfigService::getControlEntryConfig)
      .map(ControlEntryConfig::getJourneyId)
      .map(journeyService::getById)
      .map(Journey::getFriendlyJourneyName)
      .collect(Collectors.toSet());

    return ok(decontrolFurtherChecks.render(stageConfig.getStageId(), sessionId, resumeCode, currentListName, listsToCheck));
  }

  public Result handleSubmit(String stageId, String sessionId) {
    StageConfig stageConfig = controllerConfigService.getStageConfig(stageId);

    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    PageType pageType = PageTypeUtil.getPageType(stageConfig);

    switch (pageType) {
      case SELECT_ONE:
        return handleSelectOneSubmit(stageId, sessionId, stageConfig, resumeCode);
      case SELECT_MANY:
        return handleSelectManySubmit(stageId, sessionId, stageConfig, resumeCode);
      case DECONTROL:
        return handleDecontrolSubmit(stageId, sessionId, stageConfig, resumeCode);
      case ITEM:
        return handleItemSubmit(stageId, sessionId, stageConfig, resumeCode);
      case FURTHER_DECONTROL_CHECKS:
        return handleFurtherDecontrolChecksSubmit(stageId, sessionId, stageConfig, resumeCode);
      case UNKNOWN:
      default:
        throw UnknownParameterException.unknownStageId(stageId);
    }
  }

  public Result relatedEntries(String controlEntryId, String sessionId) {
    ControlEntryConfig controlEntryConfig = controllerConfigService.getControlEntryConfig(controlEntryId);

    Form<AnswerForm> answerForm = formFactory.form(AnswerForm.class);
    return renderRelatedEntries(answerForm, controlEntryConfig, sessionId);
  }

  public Result handleRelatedEntriesSubmit(String controlEntryId, String sessionId) {
    ControlEntryConfig controlEntryConfig = controllerConfigService.getControlEntryConfig(controlEntryId);

    Form<AnswerForm> answerForm = formFactory.form(AnswerForm.class).bindFromRequest();
    String actionParam = answerForm.rawData().get(ACTION);
    Action action = EnumUtil.parse(actionParam, Action.class);
    if (action == Action.CONTINUE) {
      if (answerForm.hasErrors()) {
        return renderRelatedEntries(answerForm, controlEntryConfig, sessionId);
      } else {
        String answer = answerForm.get().answer;
        List<ControlEntryConfig> controlEntryConfigs = journeyConfigService.getRelatedControlEntries(controlEntryConfig);
        List<AnswerView> answerViews = answerViewService.createAnswerViewsFromControlEntryConfigs(controlEntryConfigs);
        if (answerViews.stream().anyMatch(answerView -> answerView.getValue().equals(answer))) {
          return redirect(routes.StageController.render(answerForm.get().answer, sessionId));
        } else {
          LOGGER.warn("Answer {} not allowed in handleRelatedEntriesSubmit for controlEntryId {}", answer, controlEntryId);
          return redirectToIndex(sessionId);
        }
      }
    } else if (action == Action.NONE) {
      if (isHighLevelDropout(controlEntryConfig)) {
        return redirect(routes.OutcomeController.outcomeDropout(sessionId));
      } else {
        return redirect(routes.OutcomeController.outcomeItemNotFound(controlEntryId, sessionId));
      }
    } else {
      throw UnknownParameterException.unknownAction(actionParam);
    }
  }

  private Result handleItemSubmit(String stageId, String sessionId, StageConfig stageConfig, String resumeCode) {
    Form<AnswerForm> answerForm = formFactory.form(AnswerForm.class).bindFromRequest();
    if (answerForm.hasErrors()) {
      return renderItem(answerForm, stageConfig, sessionId, resumeCode);
    } else {
      String controlEntryId = stageConfig.getRelatedControlEntry()
        .orElseThrow(() -> new BusinessRuleException("Missing relatedControlEntry for item stage " + stageId))
        .getId();
      String answer = answerForm.get().answer;
      if ("true".equals(answer)) {
        return redirect(routes.OutcomeController.outcomeListed(controlEntryId, sessionId));
      } else if ("false".equals(answer)) {
        return resultForNoMatch(sessionId, stageConfig);
      } else {
        LOGGER.error("Unknown answer {}", answer);
        return renderItem(answerForm, stageConfig, sessionId, resumeCode);
      }
    }
  }

  private Result renderRelatedEntries(Form<AnswerForm> answerForm, ControlEntryConfig controlEntryConfig, String sessionId) {
    String controlCode = controlEntryConfig.getControlCode();
    List<ControlEntryConfig> controlEntryConfigs = journeyConfigService.getRelatedControlEntries(controlEntryConfig);
    List<AnswerView> answerViews = answerViewService.createAnswerViewsFromControlEntryConfigs(controlEntryConfigs);
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbViewFromControlEntry(sessionId, controlEntryConfig);
    return ok(relatedEntries.render(answerForm, controlEntryConfig.getId(), sessionId, resumeCode, controlCode, answerViews, breadcrumbView));
  }

  private Result renderSelectOne(Form<AnswerForm> answerForm, StageConfig stageConfig, String sessionId, String resumeCode) {
    String title = stageConfig.getQuestionTitle().orElse("Check if your item is listed");
    String explanatoryText = renderService.getExplanatoryText(stageConfig);
    List<AnswerView> answerViews = answerViewService.createAnswerViews(stageConfig, false);
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageConfig, sessionId, true);
    ProgressView progressView = progressViewService.createProgressView(stageConfig);
    return ok(selectOne.render(answerForm, stageConfig.getStageId(), sessionId, resumeCode, progressView, title, explanatoryText, answerViews, breadcrumbView));
  }

  private Result renderItem(Form<AnswerForm> answerForm, StageConfig stageConfig, String sessionId, String resumeCode) {
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageConfig, sessionId, true);
    ControlEntryConfig controlEntryConfig = breadcrumbViewService.getControlEntryConfig(stageConfig);
    String controlCode = controlEntryConfig.getControlCode();
    String description = renderService.getFullDescription(controlEntryConfig);
    List<SubAnswerView> subAnswerViews = answerViewService.createSubAnswerViews(controlEntryConfig, true);
    return ok(item.render(answerForm, stageConfig.getStageId(), sessionId, resumeCode, breadcrumbView, controlCode, description, subAnswerViews));
  }

  private Result renderDecontrol(Form<MultiAnswerForm> multiAnswerForm, StageConfig stageConfig, String sessionId,
                                 String resumeCode) {
    String title = stageConfig.getQuestionTitle().orElse("Check if your item is decontrolled");
    String explanatoryText = renderService.getExplanatoryText(stageConfig);
    List<AnswerView> answerViews = answerViewService.createAnswerViews(stageConfig, false);
    ControlEntryConfig controlEntryConfig = stageConfig.getRelatedControlEntry()
      .orElseThrow(() -> new BusinessRuleException("Missing relatedControlEntry for decontrol stage " + stageConfig.getStageId()));
    String controlCode = controlEntryConfig.getControlCode();
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageConfig, sessionId, true);

    List<String> selectedAnswers = multiAnswerForm.value().map(MultiAnswerForm::getAnswers).orElse(Collections.emptyList());
    LinkedHashMap<AnswerView, Boolean> answers = new LinkedHashMap<>();
    answerViews.forEach(answerView -> answers.put(answerView, selectedAnswers.contains(answerView.getValue())));

    return ok(decontrol.render(multiAnswerForm, stageConfig.getStageId(), sessionId, resumeCode, controlCode, title, explanatoryText, answers, breadcrumbView));
  }

  private Result renderSelectMany(Form<MultiAnswerForm> multiAnswerForm, StageConfig stageConfig, String sessionId,
                                  String resumeCode) {
    String title = stageConfig.getQuestionTitle().orElse("Check if your item is listed");
    String explanatoryText = renderService.getExplanatoryText(stageConfig);

    // Only render the related control entry description as a default when no explanatory text is provided by the CMS
    String relatedEntryDescription = null;
    Optional<ControlEntryConfig> relatedControlEntry = stageConfig.getRelatedControlEntry();
    if (stageConfig.getExplanatoryNote().isPresent() && relatedControlEntry.isPresent()) {
      relatedEntryDescription = renderService.getFullDescription(relatedControlEntry.get());
    }

    List<AnswerView> answerViews = answerViewService.createAnswerViews(stageConfig, false);
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageConfig, sessionId, true);
    ProgressView progressView = progressViewService.createProgressView(stageConfig);

    List<String> selectedAnswers = multiAnswerForm.value().map(MultiAnswerForm::getAnswers).orElse(Collections.emptyList());
    LinkedHashMap<AnswerView, Boolean> answers = new LinkedHashMap<>();
    answerViews.forEach(answerView -> answers.put(answerView, selectedAnswers.contains(answerView.getValue())));

    return ok(selectMany.render(multiAnswerForm, stageConfig.getStageId(), sessionId, resumeCode, progressView, title, explanatoryText,
      relatedEntryDescription, answers, breadcrumbView));
  }

  private Result handleDecontrolSubmit(String stageId, String sessionId, StageConfig stageConfig, String resumeCode) {
    Form<MultiAnswerForm> multiAnswerFormForm = formFactory.form(MultiAnswerForm.class).bindFromRequest();
    String actionParam = multiAnswerFormForm.rawData().get(ACTION);
    Action action = EnumUtil.parse(actionParam, Action.class);
    if (multiAnswerFormForm.hasErrors()) {
      LOGGER.error("MultiAnswerForm has unexpected errors");
      return redirectToStage(stageId, sessionId);
    } else {
      if (action == Action.CONTINUE) {
        List<String> actualAnswers = ListUtils.emptyIfNull(multiAnswerFormForm.get().getAnswers());
        List<AnswerConfig> matchingAnswers = answerConfigService.getMatchingAnswerConfigs(actualAnswers, stageConfig);
        if (matchingAnswers.isEmpty()) {
          return renderDecontrol(multiAnswerFormForm.withError("answers", "Please select at least one answer"),
            stageConfig, sessionId, resumeCode);
        } else {
          sessionService.saveAnswerIdsForStageId(sessionId, stageId, getAnswerIds(matchingAnswers));
          sessionService.updateLastStageId(sessionId, stageId);
          return redirect(routes.OutcomeController.outcomeDecontrol(stageId, sessionId));
        }
      } else if (action == Action.NONE) {
        Optional<String> nextStageId = stageConfig.getNextStageId();
        if (nextStageId.isPresent()) {
          sessionService.updateLastStageId(sessionId, stageId);
          return redirectToStage(nextStageId.get(), sessionId);
        } else if (stageConfig.getOutcomeType().orElse(null) == OutcomeType.TOO_COMPLEX) {
          String controlEntryId = stageConfig.getRelatedControlEntry()
            .map(ControlEntryConfig::getId)
            .orElseThrow(() -> new BusinessRuleException(String.format(
              "Decontrol stage %s must have an associated control entry if it has a TOO_COMPLEX outcome type", stageId)));
          return redirect(routes.OutcomeController.outcomeNoResult(controlEntryId, sessionId));
        } else {
          LOGGER.error("Decontrol stageConfig doesn't have nextStageId or applicable outcomeType");
          return redirectToStage(stageId, sessionId);
        }
      } else {
        throw UnknownParameterException.unknownAction(actionParam);
      }
    }
  }

  private Result handleSelectManySubmit(String stageId, String sessionId, StageConfig stageConfig, String resumeCode) {
    Form<MultiAnswerForm> multiAnswerFormForm = formFactory.form(MultiAnswerForm.class).bindFromRequest();
    String actionParam = multiAnswerFormForm.rawData().get(ACTION);
    Action action = EnumUtil.parse(actionParam, Action.class);
    if (multiAnswerFormForm.hasErrors()) {
      LOGGER.error("MultiAnswerForm has unexpected errors");
      return redirectToStage(stageId, sessionId);
    } else {
      if (action == Action.CONTINUE) {
        List<String> actualAnswers = ListUtils.emptyIfNull(multiAnswerFormForm.get().getAnswers());
        List<AnswerConfig> matchingAnswers = answerConfigService.getMatchingAnswerConfigs(actualAnswers, stageConfig);
        if (matchingAnswers.isEmpty()) {
          return renderSelectMany(multiAnswerFormForm.withError("answers", "Please select at least one answer"),
            stageConfig, sessionId, resumeCode);
        } else {
          AnswerConfig answerConfig = answerConfigService.getAnswerConfigWithLowestPrecedence(matchingAnswers);
          sessionService.saveAnswerIdsForStageId(sessionId, stageId, getAnswerIds(matchingAnswers));
          sessionService.updateLastStageId(sessionId, stageId);
          return resultForStandardStageAnswer(stageId, sessionId, answerConfig);
        }
      } else if (action == Action.NONE) {
        sessionService.updateLastStageId(sessionId, stageId);
        return resultForNoMatch(sessionId, stageConfig);
      } else {
        throw UnknownParameterException.unknownAction(actionParam);
      }
    }
  }

  private Set<String> getAnswerIds(List<AnswerConfig> answers) {
    return answers.stream()
      .map(AnswerConfig::getAnswerId)
      .collect(Collectors.toSet());
  }

  private Result handleFurtherDecontrolChecksSubmit(String stageId, String sessionId, StageConfig stageConfig, String resumeCode) {
    sessionService.updateLastStageId(sessionId, stageId);
    sessionService.addDecontrolledCodeFound(sessionId, stageConfig.getRelatedControlEntry().get().getControlCode());
    sessionService.addControlEntryIdsToVerifyDecontrolledStatus(sessionId, stageConfig.getRelatedControlEntry().get().getJumpToControlEntryIds());
    Optional<String> controlEntryId = sessionService.getAndRemoveControlEntryIdForDecontrolledStatusVerification(sessionId);
    String nextStageId = journeyConfigService.getStageIdsForControlEntryId(controlEntryId.get()).get(0);
    return redirectToStage(nextStageId, sessionId);
  }

  private Result handleSelectOneSubmit(String stageId, String sessionId, StageConfig stageConfig, String resumeCode) {
    Form<AnswerForm> answerForm = formFactory.form(AnswerForm.class).bindFromRequest();
    String answer = answerForm.rawData().get("answer");

    // Redirect to none of the above
    if (answer.equals("none")) {
      sessionService.updateLastStageId(sessionId, stageId);
      return resultForNoMatch(sessionId, stageConfig);
    }

    Optional<AnswerConfig> answerConfigOptional = stageConfig.getAnswerConfigs().stream()
      .filter(answerConfigIterate -> answerConfigIterate.getAnswerId().equals(answer))
      .findAny();

    if (answerConfigOptional.isPresent()) {
      AnswerConfig answerConfig = answerConfigOptional.get();
      sessionService.saveAnswerIdsForStageId(sessionId, stageId, Collections.singleton(answerConfig.getAnswerId()));
      sessionService.updateLastStageId(sessionId, stageId);
      return resultForStandardStageAnswer(stageId, sessionId, answerConfig);
    } else {
      LOGGER.error("Unknown answer {}", answer);
      return renderSelectOne(answerForm, stageConfig, sessionId, resumeCode);
    }
  }

  private Result resultForNoMatch(String sessionId, StageConfig stageConfig) {
    ControlEntryConfig controlEntryConfig = breadcrumbViewService.getControlEntryConfig(stageConfig);
    if (controlEntryConfig != null) {
      List<ControlEntryConfig> relatedControlEntries = journeyConfigService.getRelatedControlEntries(controlEntryConfig);
      if (!relatedControlEntries.isEmpty()) {
        return redirect(routes.StageController.relatedEntries(controlEntryConfig.getId(), sessionId));
      } else if (isHighLevelDropout(controlEntryConfig)) {
        return redirect(routes.OutcomeController.outcomeDropout(sessionId));
      } else {
        return redirect(controllers.routes.OutcomeController.outcomeItemNotFound(controlEntryConfig.getId(), sessionId));
      }
    } else {
      return redirect(routes.OutcomeController.outcomeDropout(sessionId));
    }
  }

  private Result resultForStandardStageAnswer(String stageId, String sessionId, AnswerConfig answerConfig) {
    Optional<OutcomeType> outcomeTypeOptional = answerConfig.getOutcomeType();
    if (outcomeTypeOptional.isPresent()) {
      OutcomeType outcomeType = outcomeTypeOptional.get();
      if (outcomeType == OutcomeType.TOO_COMPLEX) {
        String controlEntryId = answerConfig.getAssociatedControlEntryConfig()
          .map(ControlEntryConfig::getId)
          .orElseThrow(() -> new BusinessRuleException("Expected a control code to be associated with answer " +
            answerConfig.getAnswerId()));
        return redirect(routes.OutcomeController.outcomeNoResult(controlEntryId, sessionId));
      } else {
        LOGGER.error("Unexpected outcome type {} on answer {}", outcomeType, answerConfig.getAnswerId());
        return redirectToStage(stageId, sessionId);
      }
    } else {
      Optional<String> nextStageId = answerConfig.getNextStageId();
      if (nextStageId.isPresent()) {
        return redirectToStage(nextStageId.get(), sessionId);
      } else {
        LOGGER.error("AnswerConfig doesn't have next stageId.");
        return redirectToStage(stageId, sessionId);
      }
    }
  }

  private boolean isHighLevelDropout(ControlEntryConfig controlEntryConfig) {
    // Business rule: "top level" control entries (e.g. ML1, ML2) are considered too high level for an NLR outcome
    return !controlEntryConfig.getParentControlEntry().isPresent();
  }

  private Result redirectToIndex(String sessionId) {
    String initialStageId = journeyConfigService.getStageConfigForInitialJourneyStage(
      controllerConfigService.getStageConfig(Long.toString(sessionService.getSessionById(sessionId).getLastStageId()))
        .getJourneyId()).getStageId();
    return redirect(routes.StageController.render(initialStageId, sessionId));
  }

  private Result redirectToStage(String stageId, String sessionId) {
    return redirect(routes.StageController.render(stageId, sessionId));
  }
}
