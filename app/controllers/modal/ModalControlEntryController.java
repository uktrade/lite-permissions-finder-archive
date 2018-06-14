package controllers.modal;

import com.google.inject.Inject;
import components.services.AnswerViewService;
import components.services.BreadcrumbViewService;
import models.view.BreadcrumbItemView;
import models.view.SubAnswerView;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.text.HtmlRenderOption;
import triage.text.HtmlRenderService;
import views.html.modal.modalControlEntry;

import java.util.List;

public class ModalControlEntryController extends Controller {
  private final JourneyConfigService journeyConfigService;
  private final BreadcrumbViewService breadcrumbViewService;
  private final AnswerViewService answerViewService;
  private final HtmlRenderService htmlRenderService;
  private final views.html.modal.modalControlEntryView modalControlEntryView;

  @Inject
  public ModalControlEntryController(JourneyConfigService journeyConfigService,
                                     BreadcrumbViewService breadcrumbViewService,
                                     AnswerViewService answerViewService,
                                     HtmlRenderService htmlRenderService,
                                     views.html.modal.modalControlEntryView modalControlEntryView) {
    this.journeyConfigService = journeyConfigService;
    this.breadcrumbViewService = breadcrumbViewService;
    this.answerViewService = answerViewService;
    this.htmlRenderService = htmlRenderService;
    this.modalControlEntryView = modalControlEntryView;
  }

  public Result renderControlEntryModal(String controlEntryId, String sessionId) {
    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(controlEntryId);
    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(null, controlEntryConfig);
    String controlEntryUrl = createGoToControlEntryUrl(controlEntryConfig, sessionId);
    String description = createDescription(controlEntryConfig);
    return ok(modalControlEntry.render(controlEntryConfig.getControlCode(), breadcrumbItemViews, controlEntryUrl, description));
  }

  public Result renderControlEntryView(String controlEntryId) {
    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(controlEntryId);
    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(null, controlEntryConfig, HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    String description = createDescription(controlEntryConfig);
    return ok(modalControlEntryView.render(controlEntryConfig.getControlCode(), breadcrumbItemViews, description));
  }

  private String createDescription(ControlEntryConfig controlEntryConfig) {
    List<ControlEntryConfig> controlEntryConfigs = journeyConfigService.getChildRatings(controlEntryConfig);
    if (controlEntryConfigs.isEmpty()) {
      return "";
    } else {
      StringBuilder builder = new StringBuilder();
      builder.append("<ul>");
      controlEntryConfigs.forEach(controlEntryConfigIterate -> {
        builder.append("<li>");
        builder.append(htmlRenderService.convertRichTextToHtml(controlEntryConfigIterate.getFullDescription()));
        builder.append("</li>");
        List<SubAnswerView> subAnswerViews = answerViewService.createSubAnswerViews(controlEntryConfigIterate, true);
        builder.append(answerViewService.createSubAnswerViewsHtml(subAnswerViews));
      });
      builder.append("</ul>");
      return builder.toString();
    }
  }

  private String createGoToControlEntryUrl(ControlEntryConfig controlEntryConfig, String sessionId) {
    return journeyConfigService.getPrincipleStageConfigForControlEntry(controlEntryConfig)
        .map(stageConfig -> controllers.routes.StageController.render(stageConfig.getStageId(), sessionId).toString())
        .orElseGet(() -> {
          Logger.info("Cannot create \"Go to this control entry\" link for invalid controlEntryId " + controlEntryConfig.getId());
          return null;
        });
  }
}
