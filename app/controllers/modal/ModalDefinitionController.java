package controllers.modal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import lombok.AllArgsConstructor;
import play.mvc.Result;
import triage.config.ControllerConfigService;
import triage.config.DefinitionConfig;
import triage.text.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.mvc.Results.ok;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class ModalDefinitionController {

  private final HtmlRenderService htmlRenderService;
  private final ControllerConfigService controllerConfigService;
  private final views.html.modal.modalDefinitionView modalDefinitionView;


  public Result renderDefinition(String type, String definitionId) {
    Map<String, String> definition = getDefinition(type, definitionId);
    return ok(Json.prettyPrint(new ObjectMapper().valueToTree(definition)));
  }

  public Result renderDefinitionView(String type, String definitionId, Boolean showBackLink) {
    Map<String, String> definition = getDefinition(type, definitionId);

    // Add showBackLink to all URLs
    String definitionText = definition.get("definition");
    definitionText = definitionText.replace("\" class=\"govuk-link dotted-link", "?showBackLink=true\" class=\"govuk-link dotted-link");

    return ok(modalDefinitionView.render(definition.get("term"), definitionText,
      showBackLink));
  }

  private Map<String, String> getDefinition(String type, String definitionId) {
    Map<String, String> result = new HashMap<>();
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

    result.put("term", StringUtils.capitalize(term));
    result.put("definition", "<p class='govuk-label'>" + definition + "</p>");

    return result;
  }
}
