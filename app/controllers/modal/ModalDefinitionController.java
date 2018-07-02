package controllers.modal;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import play.mvc.Result;
import triage.config.DefinitionConfig;
import triage.config.DefinitionConfigService;
import triage.text.HtmlRenderOption;
import triage.text.HtmlRenderService;
import views.html.modal.modalDefinition;

public class ModalDefinitionController {

  private final HtmlRenderService htmlRenderService;
  private final DefinitionConfigService definitionConfigService;
  private final views.html.modal.modalDefinitionView modalDefinitionView;

  @Inject
  public ModalDefinitionController(HtmlRenderService htmlRenderService, DefinitionConfigService definitionConfigService,
                                   views.html.modal.modalDefinitionView modalDefinitionView) {
    this.htmlRenderService = htmlRenderService;
    this.definitionConfigService = definitionConfigService;
    this.modalDefinitionView = modalDefinitionView;
  }

  public Result renderGlobalDefinition(String globalDefinitionId) {
    DefinitionConfig globalDefinition = definitionConfigService.getGlobalDefinitionNotNull(globalDefinitionId);

    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(globalDefinition.getDefinitionText());
    return ok(modalDefinition.render(globalDefinition.getTerm(), definitionTextHtml));
  }

  public Result renderGlobalDefinitionView(String globalDefinitionId) {
    DefinitionConfig globalDefinition = definitionConfigService.getGlobalDefinitionNotNull(globalDefinitionId);

    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(globalDefinition.getDefinitionText(),
        HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    return ok(modalDefinitionView.render(globalDefinition.getTerm(), definitionTextHtml));
  }

  public Result renderLocalDefinition(String localDefinitionId) {
    DefinitionConfig localDefinition = definitionConfigService.getLocalDefinitionNotNull(localDefinitionId);

    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(localDefinition.getDefinitionText());
    return ok(modalDefinition.render(localDefinition.getTerm(), definitionTextHtml));
  }

  public Result renderLocalDefinitionView(String localDefinitionId) {
    DefinitionConfig localDefinition = definitionConfigService.getLocalDefinitionNotNull(localDefinitionId);

    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(localDefinition.getDefinitionText(),
        HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    return ok(modalDefinitionView.render(localDefinition.getTerm(), definitionTextHtml));
  }

}
