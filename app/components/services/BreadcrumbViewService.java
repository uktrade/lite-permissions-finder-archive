package components.services;

import models.view.BreadcrumbView;
import triage.config.ControlEntryConfig;

public interface BreadcrumbViewService {
  BreadcrumbView createBreadcrumbView(String stageId);

  BreadcrumbView createBreadcrumbView(ControlEntryConfig controlEntryConfig);
}
