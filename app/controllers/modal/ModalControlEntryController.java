package controllers.modal;

import com.google.inject.Inject;
import components.services.BreadcrumbViewService;
import models.view.BreadcrumbItemView;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.text.HtmlRenderOption;
import views.html.modal.modalControlEntry;

import java.util.List;

public class ModalControlEntryController extends Controller {
  private final JourneyConfigService journeyConfigService;
  private final BreadcrumbViewService breadcrumbViewService;
  private final views.html.modal.modalControlEntryView modalControlEntryView;

  @Inject
  public ModalControlEntryController(JourneyConfigService journeyConfigService,
                                     BreadcrumbViewService breadcrumbViewService,
                                     views.html.modal.modalControlEntryView modalControlEntryView) {
    this.journeyConfigService = journeyConfigService;
    this.breadcrumbViewService = breadcrumbViewService;
    this.modalControlEntryView = modalControlEntryView;
  }

  public Result renderControlEntryModal(String controlEntryId, String sessionId) {
    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(controlEntryId);
    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(null, controlEntryConfig);
    String controlEntryUrl = createGoToControlEntryUrl(controlEntryConfig, sessionId);
    return ok(modalControlEntry.render(controlEntryConfig.getControlCode(), breadcrumbItemViews, controlEntryUrl));
  }

  public Result renderControlEntryView(String controlEntryId) {
    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(controlEntryId);
    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(null, controlEntryConfig, HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    return ok(modalControlEntryView.render(controlEntryConfig.getControlCode(), breadcrumbItemViews));
  }

  private String createGoToControlEntryUrl(ControlEntryConfig controlEntryConfig, String sessionId) {
    List<String> stageIds = journeyConfigService.getStageIdsForControlEntry(controlEntryConfig);
    String stageId = stageIds.stream()
        .map(journeyConfigService::getStageConfigById)
        .filter(stageConfigIterate -> stageConfigIterate.getQuestionType() == StageConfig.QuestionType.STANDARD)
        .findAny()
        .map(stageConfig -> stageConfig.getStageId())
        .orElse("TODO");

    if (!stageId.equals("TODO")) {
      return controllers.routes.StageController.render(stageId, sessionId).toString();
    } else {
      Logger.info("Cannot create \"Go to this control entry\" link for invalid stageId " + stageId);
      return null;
    }
  }
}
