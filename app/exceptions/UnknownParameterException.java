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

}
