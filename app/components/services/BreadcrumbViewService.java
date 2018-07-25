package components.services;

import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import models.view.RelatedEntryView;
import triage.config.ControlEntryConfig;
import triage.config.StageConfig;
import triage.text.HtmlRenderOption;

import java.util.List;

public interface BreadcrumbViewService {

  BreadcrumbView createBreadcrumbViewFromControlEntry(String sessionId, ControlEntryConfig controlEntryConfig);

  BreadcrumbView createBreadcrumbView(StageConfig stageConfig, String sessionId, boolean includeChangeLinks,
                                      HtmlRenderOption... htmlRenderOptions);

  ControlEntryConfig getControlEntryConfig(StageConfig stageConfig);

  List<BreadcrumbItemView> createBreadcrumbItemViews(String sessionId, ControlEntryConfig controlEntryConfig,
                                                     boolean includeChangeLinks, HtmlRenderOption... htmlRenderOptions);

  String createDecontrolUrl(String sessionId, ControlEntryConfig controlEntryConfig);

  List<RelatedEntryView> createRelatedEntryViews(String sessionId, ControlEntryConfig controlEntryConfig,
                                                 boolean includeChangeLinks, HtmlRenderOption... htmlRenderOptions);
}
