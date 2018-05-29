package models.view;

import org.apache.commons.lang.StringUtils;

public class RegisterResultView {

  private final String title;
  private final String registrationRef;

  public RegisterResultView(String title, String registrationRef) {
    this.title = title;
    this.registrationRef = registrationRef;
  }

  public RegisterResultView(String title) {
    this.title = title;
    this.registrationRef = null;
  }

  public String getTitle() {
    return title;
  }

  public String getRegistrationRef() {
    return registrationRef;
  }

  public boolean hasRegistrationRef() {
    return !StringUtils.isBlank(registrationRef);
  }
}
