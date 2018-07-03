package models.persistence;

public class RegisterLicence {

  private String sessionId;
  private String requestId;
  private String registrationReference;
  private String userId;
  private String customerId;
  private String siteId;
  private String ogelId;
  private String callbackUrl;

  private String userEmailAddress;
  private String userFullName;

  public RegisterLicence() {}

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getSiteId() {
    return siteId;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  public String getOgelId() {
    return ogelId;
  }

  public void setOgelId(String ogelId) {
    this.ogelId = ogelId;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getRegistrationReference() {
    return registrationReference;
  }

  public void setRegistrationReference(String registrationReference) {
    this.registrationReference = registrationReference;
  }

  public String getUserEmailAddress() {
    return userEmailAddress;
  }

  public void setUserEmailAddress(String userEmailAddress) {
    this.userEmailAddress = userEmailAddress;
  }

  public String getUserFullName() {
    return userFullName;
  }

  public void setUserFullName(String userFullName) {
    this.userFullName = userFullName;
  }
}
