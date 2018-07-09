package models.template;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class AnalyticsSnippets {
  private final String headJs;
  private final String bodyHtml;

  @Inject
  public AnalyticsSnippets(@Named("analyticsHeadJs") String headJs, @Named("analyticsBodyHtml") String bodyHtml) {
    this.headJs = headJs;
    this.bodyHtml = bodyHtml;
  }

  public String getHeadJs() {
    return headJs;
  }

  public String getBodyHtml() {
    return bodyHtml;
  }
}
