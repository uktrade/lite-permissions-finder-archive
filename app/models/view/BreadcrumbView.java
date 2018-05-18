package models.view;

import java.util.List;

public class BreadcrumbView {

  private final List<BreadcrumbItemView> breadcrumbItemViews;

  public BreadcrumbView(List<BreadcrumbItemView> breadcrumbItemViews) {
    this.breadcrumbItemViews = breadcrumbItemViews;
  }

  public List<BreadcrumbItemView> getBreadcrumbItemViews() {
    return breadcrumbItemViews;
  }

}
