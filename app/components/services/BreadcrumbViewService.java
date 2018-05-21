package components.services;

import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import triage.config.ControlEntryConfig;

import java.util.List;

public interface BreadcrumbViewService {

  BreadcrumbView createBreadcrumbView(String stageId);

  List<BreadcrumbItemView> createBreadcrumbItemViews(ControlEntryConfig controlEntryConfig);

}
