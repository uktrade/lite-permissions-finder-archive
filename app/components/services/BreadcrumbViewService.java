package components.services;

import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import triage.config.ControlEntryConfig;
import triage.config.StageConfig;

import java.util.List;

public interface BreadcrumbViewService {

  BreadcrumbView createBreadcrumbView(String stageId);

  ControlEntryConfig getControlEntryConfig(StageConfig stageConfig);

  List<BreadcrumbItemView> createBreadcrumbItemViews(ControlEntryConfig controlEntryConfig);

}
