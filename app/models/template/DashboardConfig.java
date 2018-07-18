package models.template;

public class DashboardConfig {
  private final String dashboardUrl;

  public DashboardConfig(String dashboardUrl) {
    this.dashboardUrl = dashboardUrl;
  }

  public String getDashboardUrl() {
    return dashboardUrl;
  }
}
