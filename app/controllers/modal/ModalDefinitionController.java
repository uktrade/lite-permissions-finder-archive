package controllers.modal;

import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import play.mvc.Result;
import triage.config.ControllerConfigService;
import triage.config.DefinitionConfig;
import triage.text.*;

import java.util.List;
import java.util.stream.Collectors;

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

  public Result renderDefinition(String type, String definitionId) {
    String term = "Unknown term";
    String definition = "Unknown definition";
    DefinitionConfig definitionConfig = null;

    // Set definition config depending on definition type
    switch (type) {
      case "global":
        definitionConfig = controllerConfigService.getGlobalDefinitionConfig(definitionId);
        break;
      case "local":
        definitionConfig = controllerConfigService.getLocalDefinitionConfig(definitionId);
        break;
    }

    // Set term and definition
    if (definitionConfig != null) {
      term = definitionConfig.getTerm();

      List<RichTextNode> nodes = definitionConfig.getDefinitionText().getRichTextNodes();

      // Replace term text with simple node
      for (int i = 0; i < nodes.size(); i++) {
        RichTextNode richTextNode = nodes.get(i);
        if (richTextNode instanceof DefinitionReferenceNode) {
          if (richTextNode.getTextContent().equalsIgnoreCase("\"" + definitionConfig.getTerm() + "\"")) {
            nodes.set(i, new SimpleTextNode(definitionConfig.getTerm()));
          }
        }
      }

      definitionConfig.getDefinitionText().setRichTextNodes(nodes);

      definition = htmlRenderService.convertRichTextToHtml(definitionConfig.getDefinitionText(),
              HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    }

    // Return JSON
    ObjectNode result = Json.newObject();
    result.put("term", StringUtils.capitalize(term));
    result.put("definition", "<p class='govuk-label'>" + definition + "</p>");
    return ok(Json.prettyPrint(result));
  }

  public Result renderGlobalDefinitionView(String globalDefinitionId) {
    DefinitionConfig globalDefinition = controllerConfigService.getGlobalDefinitionConfig(globalDefinitionId);

    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(globalDefinition.getDefinitionText(),
        HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    return ok(modalDefinitionView.render(StringUtils.capitalize(globalDefinition.getTerm()), definitionTextHtml));
  }

  public Result renderLocalDefinitionView(String localDefinitionId) {
    DefinitionConfig localDefinition = controllerConfigService.getLocalDefinitionConfig(localDefinitionId);

    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(localDefinition.getDefinitionText(),
        HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    return ok(modalDefinitionView.render(StringUtils.capitalize(localDefinition.getTerm()), definitionTextHtml));
  }

}
