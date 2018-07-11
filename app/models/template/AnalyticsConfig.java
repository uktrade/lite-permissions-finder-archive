package models.template;

public class AnalyticsConfig {
  private final String googleAnalyticsId;

  public AnalyticsConfig(String googleAnalyticsId) {
    this.googleAnalyticsId = googleAnalyticsId;
  }

  public String getGoogleAnalyticsId() {
    return googleAnalyticsId;
  }
}
