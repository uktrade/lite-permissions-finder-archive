package components.services;

import com.google.inject.Inject;
import triage.config.ControlEntryConfig;
import triage.config.StageConfig;
import triage.text.HtmlRenderService;
import triage.text.RichText;

import java.util.Optional;

public class RenderServiceImpl implements RenderService {

  private final HtmlRenderService htmlRenderService;

  @Inject
  public RenderServiceImpl(HtmlRenderService htmlRenderService) {
    this.htmlRenderService = htmlRenderService;
  }

  @Override
  public String getExplanatoryText(StageConfig stageConfig) {
    Optional<RichText> explanatoryNoteOptional = stageConfig.getExplanatoryNote();
    if (explanatoryNoteOptional.isPresent()) {
      RichText explanatoryNote = explanatoryNoteOptional.get();
      return htmlRenderService.convertRichTextToHtml(explanatoryNote);
    } else {
      return null;
    }
  }

  @Override
  public String getFullDescription(ControlEntryConfig controlEntryConfig) {
    return htmlRenderService.convertRichTextToPlainText(controlEntryConfig.getFullDescription());
  }

  @Override
  public String getSummaryDescription(ControlEntryConfig controlEntryConfig) {
    Optional<RichText> summaryDescription = controlEntryConfig.getSummaryDescription();
    if (summaryDescription.isPresent()) {
      RichText richText = summaryDescription.get();
      return htmlRenderService.convertRichTextToHtml(richText);
    } else {
      return null;
    }
  }

}
