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
    return new UnknownParameterException("Unknown modalContentId: " + modalContentId);
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

  public static UnknownParameterException unknownDestinationCountry(String destinationCountry) {
    return new UnknownParameterException("Unknown destinationCountry " + destinationCountry);
  }

  public static UnknownParameterException unknownFirstConsigneeCountry(String firstConsigneeCountry) {
    return new UnknownParameterException("Unknown firstConsigneeCountry " + firstConsigneeCountry);
  }

  public static UnknownParameterException unknownOgelRegistrationOrder() {
    return new UnknownParameterException("Unknown ogel registration order");
  }

}
