package controllers.modal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import components.services.AnswerViewService;
import components.services.BreadcrumbViewService;
import models.view.BreadcrumbItemView;
import models.view.SubAnswerView;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import triage.config.ControlEntryConfig;
import triage.config.ControllerConfigService;
import triage.config.JourneyConfigService;
import triage.text.HtmlRenderOption;
import triage.text.HtmlRenderService;
import views.html.modal.modalControlEntry;

import java.util.List;

public class ModalControlEntryController extends Controller {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ModalControlEntryController.class);

  private final JourneyConfigService journeyConfigService;
  private final ControllerConfigService controllerConfigService;
  private final BreadcrumbViewService breadcrumbViewService;
  private final AnswerViewService answerViewService;
  private final HtmlRenderService htmlRenderService;
  private final views.html.modal.modalControlEntryView modalControlEntryView;

  @Inject
  public ModalControlEntryController(JourneyConfigService journeyConfigService,
                                     ControllerConfigService controllerConfigService,
                                     BreadcrumbViewService breadcrumbViewService,
                                     AnswerViewService answerViewService,
                                     HtmlRenderService htmlRenderService,
                                     views.html.modal.modalControlEntryView modalControlEntryView) {
    this.journeyConfigService = journeyConfigService;
    this.controllerConfigService = controllerConfigService;
    this.breadcrumbViewService = breadcrumbViewService;
    this.answerViewService = answerViewService;
    this.htmlRenderService = htmlRenderService;
    this.modalControlEntryView = modalControlEntryView;
  }

  public Result renderControlEntryModal(String controlEntryId, String sessionId) {
    ControlEntryConfig controlEntryConfig = controllerConfigService.getControlEntryConfig(controlEntryId);
    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(null, controlEntryConfig, true);

    // Convert object to JSON
    ObjectNode result = Json.newObject();
    ArrayNode array = new ObjectMapper().valueToTree(breadcrumbItemViews);
    result.put("items", array);
    result.put("controlEntryUrl", createGoToControlEntryUrl(controlEntryConfig, sessionId));
    result.put("description", createDescription(controlEntryConfig));

    return ok(Json.prettyPrint(result));
  }

  public Result renderControlEntryView(String controlEntryId) {
    ControlEntryConfig controlEntryConfig = controllerConfigService.getControlEntryConfig(controlEntryId);

    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(null, controlEntryConfig, true,
        HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    String description = createDescription(controlEntryConfig);

    // Add showBackLink to all URLs
    description = description.replace("\" class=\"govuk-link dotted-link", "?showBackLink=true\" class=\"govuk-link dotted-link");

    return ok(modalControlEntryView.render(controlEntryConfig.getControlCode(), breadcrumbItemViews, description));
  }

  private String createDescription(ControlEntryConfig controlEntryConfig) {
    List<ControlEntryConfig> controlEntryConfigs = journeyConfigService.getChildRatings(controlEntryConfig);
    if (controlEntryConfigs.isEmpty()) {
      return "";
    } else {
      StringBuilder builder = new StringBuilder();
      builder.append("<ul class='govuk-list govuk-list--bullet'>");
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
          LOGGER.info("Cannot create \"Go to this control entry\" link for invalid controlEntryId {}", controlEntryConfig.getId());
          return null;
        });
  }

}
