package triage.session;


import models.enums.SessionOutcomeType;

public class SessionOutcome {

  private final String id;
  private final String sessionId;
  private final String userId;
  private final String customerId;
  private final String siteId;
  private final SessionOutcomeType outcomeType;
  private final String outcomeHtml;

  public SessionOutcome(String id, String sessionId, String userId, String customerId, String siteId,
                        SessionOutcomeType outcomeType, String outcomeHtml) {
    this.id = id;
    this.sessionId = sessionId;
    this.userId = userId;
    this.customerId = customerId;
    this.siteId = siteId;
    this.outcomeType = outcomeType;
    this.outcomeHtml = outcomeHtml;
  }

  public String getId() {
    return id;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getUserId() {
    return userId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public String getSiteId() {
    return siteId;
  }

  public SessionOutcomeType getOutcomeType() {
    return outcomeType;
  }

  public String getOutcomeHtml() {
    return outcomeHtml;
  }

}
