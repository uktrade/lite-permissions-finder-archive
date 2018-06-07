package triage.session;


import models.enums.OutcomeType;

public class SessionOutcome {

  private final Long id;
  private final String sessionId;
  private final String userId;
  private final String customerId;
  private final String siteId;
  private final OutcomeType outcomeType;
  private final String outcomeHtml;

  public SessionOutcome(Long id, String sessionId, String userId, String customerId, String siteId,
                        OutcomeType outcomeType, String outcomeHtml) {
    this.id = id;
    this.sessionId = sessionId;
    this.userId = userId;
    this.customerId = customerId;
    this.siteId = siteId;
    this.outcomeType = outcomeType;
    this.outcomeHtml = outcomeHtml;
  }

  public Long getId() {
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

  public OutcomeType getOutcomeType() {
    return outcomeType;
  }

  public String getOutcomeHtml() {
    return outcomeHtml;
  }

}
