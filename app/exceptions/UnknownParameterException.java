package exceptions;

public class UnknownParameterException extends RuntimeException {

  private UnknownParameterException(String message) {
    super(message);
  }

  public static UnknownParameterException unknownStageId(String stageId) {
    return new UnknownParameterException("Unknown stageId " + stageId);
  }

  public static UnknownParameterException unknownControlEntryId(String controlEntryId) {
    return new UnknownParameterException("Unknown controlEntryId " + controlEntryId);
  }

  public static UnknownParameterException unknownControlCode(String controlCode) {
    return new UnknownParameterException("Unknown controlCode " + controlCode);
  }

  public static UnknownParameterException unknownAction(String action) {
    return new UnknownParameterException("Unknown action " + action);
  }

  public static UnknownParameterException unknownModalContentId(String modalContentId) {
    return new UnknownParameterException("Unknown modalContentId " + modalContentId);
  }

  public static UnknownParameterException unknownGlobalDefinitionId(String globalDefinitionId) {
    return new UnknownParameterException("Unknown globalDefinitionId " + globalDefinitionId);
  }

  public static UnknownParameterException unknownLocalDefinitionId(String localDefinitionId) {
    return new UnknownParameterException("Unknown localDefinitionId " + localDefinitionId);
  }

  public static UnknownParameterException unknownOgelReference(String ogelReference) {
    return new UnknownParameterException("Unknown ogelReference " + ogelReference);
  }

  public static UnknownParameterException unknownResumeCode(String resumeCode) {
    return new UnknownParameterException("Unknown resume code " + resumeCode);
  }

  public static UnknownParameterException unknownTradeType(String tradeType) {
    return new UnknownParameterException("Unknown tradeType " + tradeType);
  }

  public static UnknownParameterException unknownCountry(String country) {
    return new UnknownParameterException("Unknown country " + country);
  }

  public static UnknownParameterException unknownLicenceFinderOrder() {
    return new UnknownParameterException("Unknown licenceFinderOrder");
  }

  public static UnknownParameterException unknownOutcomeId(String outcomeId) {
    return new UnknownParameterException("Unknown outcomeId " + outcomeId);
  }

  public static UnknownParameterException unknownOutcomeForSessionId(String sessionId) {
    return new UnknownParameterException("Unknown outcome for sessionId " + sessionId);
  }

}
