package models.view;

import java.util.List;

public class BreadcrumbView {

  private final List<BreadcrumbItemView> breadcrumbItemViews;
  private final boolean decontrol;

  public BreadcrumbView(List<BreadcrumbItemView> breadcrumbItemViews, boolean decontrol) {
    this.breadcrumbItemViews = breadcrumbItemViews;
    this.decontrol = decontrol;
  }

  public List<BreadcrumbItemView> getBreadcrumbItemViews() {
    return breadcrumbItemViews;
  }

  public boolean isDecontrol() {
    return decontrol;
  }

}
