package controllers.modal;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import play.mvc.Result;
import triage.config.DefinitionConfig;
import triage.config.DefinitionConfigService;
import triage.text.HtmlRenderService;
import views.html.modal.modalDefinition;

public class ModalDefinitionController {
  private final HtmlRenderService htmlRenderService;
  private final DefinitionConfigService definitionConfigService;

  @Inject
  public ModalDefinitionController(HtmlRenderService htmlRenderService,
                                   DefinitionConfigService definitionConfigService) {
    this.htmlRenderService = htmlRenderService;
    this.definitionConfigService = definitionConfigService;
  }


  public Result renderGlobalDefinition(String globalDefinitionId) {
    DefinitionConfig definitionConfig = definitionConfigService.getGlobalDefinition(globalDefinitionId);
    String definitionTextHtml = htmlRenderService.convertRichText(definitionConfig.getDefinitionText(), true);
    return ok(modalDefinition.render(definitionConfig.getTerm(), definitionTextHtml));
  }

  public Result renderLocalDefinition(String localDefinitionId) {
    DefinitionConfig definitionConfig = definitionConfigService.getLocalDefinition(localDefinitionId);
    String definitionTextHtml = htmlRenderService.convertRichText(definitionConfig.getDefinitionText(), true);
    return ok(modalDefinition.render(definitionConfig.getTerm(), definitionTextHtml));
  }
}
