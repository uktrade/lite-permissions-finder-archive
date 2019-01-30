package controllers.modal;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import play.mvc.Result;
import triage.config.ControllerConfigService;
import triage.config.DefinitionConfig;
import triage.text.HtmlRenderOption;
import triage.text.HtmlRenderService;
import views.html.modal.modalDefinition;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class ModalDefinitionController {

  private final HtmlRenderService htmlRenderService;
  private final ControllerConfigService controllerConfigService;
  private final views.html.modal.modalDefinitionView modalDefinitionView;

  public Result renderGlobalDefinition(String globalDefinitionId) {
    DefinitionConfig globalDefinition = controllerConfigService.getGlobalDefinitionConfig(globalDefinitionId);

    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(globalDefinition.getDefinitionText());
    return ok(modalDefinition.render(globalDefinition.getTerm(), definitionTextHtml));
  }

  public Result renderGlobalDefinitionView(String globalDefinitionId) {
    DefinitionConfig globalDefinition = controllerConfigService.getGlobalDefinitionConfig(globalDefinitionId);

    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(globalDefinition.getDefinitionText(),
        HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    return ok(modalDefinitionView.render(globalDefinition.getTerm(), definitionTextHtml));
  }

  public Result renderLocalDefinition(String localDefinitionId) {
    DefinitionConfig localDefinition = controllerConfigService.getLocalDefinitionConfig(localDefinitionId);

    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(localDefinition.getDefinitionText());
    return ok(modalDefinition.render(localDefinition.getTerm(), definitionTextHtml));
  }

  public Result renderLocalDefinitionView(String localDefinitionId) {
    DefinitionConfig localDefinition = controllerConfigService.getLocalDefinitionConfig(localDefinitionId);

    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(localDefinition.getDefinitionText(),
        HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    return ok(modalDefinitionView.render(localDefinition.getTerm(), definitionTextHtml));
  }

}
