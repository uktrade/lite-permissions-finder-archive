package components.services;

import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import triage.config.ControlEntryConfig;
import triage.config.StageConfig;
import triage.text.HtmlRenderOption;

import java.util.List;

public interface BreadcrumbViewService {

  BreadcrumbView createBreadcrumbViewFromControlEntryId(String sessionId, String controlEntryId);

  BreadcrumbView createBreadcrumbView(String stageId, String sessionId, boolean includeChangeLinks,
                                      HtmlRenderOption... htmlRenderOptions);

  ControlEntryConfig getControlEntryConfig(StageConfig stageConfig);

  List<BreadcrumbItemView> createBreadcrumbItemViews(String sessionId, ControlEntryConfig controlEntryConfig,
                                                     boolean includeChangeLinks, HtmlRenderOption... htmlRenderOptions);

  String createDecontrolUrl(String sessionId, ControlEntryConfig controlEntryConfig);
}
