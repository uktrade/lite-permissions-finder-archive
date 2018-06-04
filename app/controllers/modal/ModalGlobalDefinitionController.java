package controllers.modal;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.cms.dao.GlobalDefinitionDao;
import models.cms.GlobalDefinition;
import play.mvc.Result;
import triage.text.HtmlRenderService;
import triage.text.RichText;
import triage.text.RichTextParser;
import views.html.modal.modalGlobalDefinition;

public class ModalGlobalDefinitionController {
  private final GlobalDefinitionDao globalDefinitionDao;
  private final RichTextParser richTextParser;
  private final HtmlRenderService htmlRenderService;

  @Inject
  public ModalGlobalDefinitionController(GlobalDefinitionDao globalDefinitionDao, RichTextParser richTextParser, HtmlRenderService htmlRenderService) {
    this.globalDefinitionDao = globalDefinitionDao;
    this.richTextParser = richTextParser;
    this.htmlRenderService = htmlRenderService;
  }

  public Result renderGlobalDefinition(String globalDefinitionId) {
    GlobalDefinition globalDefinition = globalDefinitionDao.getGlobalDefinition(Long.parseLong(globalDefinitionId));
    RichText richDefinitionText = richTextParser.parseForStage(globalDefinition.getDefinitionText(), null);
    String definitionTextHtml = htmlRenderService.convertRichTextToHtml(richDefinitionText);
    return ok(modalGlobalDefinition.render(globalDefinition.getTerm(), definitionTextHtml));
  }
}
