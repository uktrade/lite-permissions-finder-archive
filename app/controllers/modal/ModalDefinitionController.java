package controllers.modal;

import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import play.libs.Json;
import play.mvc.Result;
import triage.config.ControllerConfigService;
import triage.config.DefinitionConfig;
import triage.text.HtmlRenderOption;
import triage.text.HtmlRenderService;
import views.html.modal.modalDefinition;

public class ModalDefinitionController {

  private final HtmlRenderService htmlRenderService;
  private final ControllerConfigService controllerConfigService;
  private final views.html.modal.modalDefinitionView modalDefinitionView;

  @Inject
  public ModalDefinitionController(HtmlRenderService htmlRenderService,
                                   ControllerConfigService controllerConfigService,
                                   views.html.modal.modalDefinitionView modalDefinitionView) {
    this.htmlRenderService = htmlRenderService;
    this.controllerConfigService = controllerConfigService;
    this.modalDefinitionView = modalDefinitionView;
  }

  public Result renderGlobalDefinition(String globalDefinitionId) {
    DefinitionConfig globalDefinition = controllerConfigService.getGlobalDefinitionConfig(globalDefinitionId);

    ObjectNode result = Json.newObject();
    result.put("term", globalDefinition.getTerm());
    result.put("definition", htmlRenderService.convertRichTextToHtml(globalDefinition.getDefinitionText(),
            HtmlRenderOption.OMIT_LINK_TARGET_ATTR));

    return ok(Json.prettyPrint(result));
  }

  public Result renderGlobalDefinitionView(String globalDefinitionId) {
    DefinitionConfig globalDefinition = controllerConfigService.getGlobalDefinitionConfig(globalDefinitionId);

    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(globalDefinition.getDefinitionText(),
        HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    return ok(modalDefinitionView.render(globalDefinition.getTerm(), definitionTextHtml));
  }

  public Result renderLocalDefinition(String localDefinitionId) {
    DefinitionConfig localDefinition = controllerConfigService.getLocalDefinitionConfig(localDefinitionId);

    ObjectNode result = Json.newObject();
    result.put("term", localDefinition.getTerm());
    result.put("definition", htmlRenderService.convertRichTextToHtml(localDefinition.getDefinitionText(),
            HtmlRenderOption.OMIT_LINK_TARGET_ATTR));

    return ok(Json.prettyPrint(result));
  }

  public Result renderLocalDefinitionView(String localDefinitionId) {
    DefinitionConfig localDefinition = controllerConfigService.getLocalDefinitionConfig(localDefinitionId);

    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(localDefinition.getDefinitionText(),
        HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    return ok(modalDefinitionView.render(localDefinition.getTerm(), definitionTextHtml));
  }

}
