package components.services;

import triage.config.ControlEntryConfig;
import triage.config.StageConfig;
import triage.text.HtmlRenderOption;

public interface RenderService {
  String getExplanatoryText(StageConfig stageConfig, HtmlRenderOption... htmlRenderOptions);

  String getFullDescription(ControlEntryConfig controlEntryConfig, HtmlRenderOption... htmlRenderOptions);

  String getSummaryDescription(ControlEntryConfig controlEntryConfig, HtmlRenderOption... htmlRenderOptions);

}
