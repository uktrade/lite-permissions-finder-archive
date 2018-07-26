package models.view.licencefinder;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class NotFound {
  private final String permissionsFinderUrl;

  @Inject
  public NotFound(@Named("permissionsFinderUrl") String permissionsFinderUrl) {
    this.permissionsFinderUrl = permissionsFinderUrl;
  }

  public String getPermissionsFinderUrl() {
    return permissionsFinderUrl;
  }
}
