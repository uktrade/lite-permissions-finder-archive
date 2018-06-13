package components.services;

public interface SessionOutcomeService {

  void generateItemListedOutcome(String userId, String sessionId, String controlEntryId);

  String generateNotFoundNlrLetter(String userId, String sessionId, String controlEntryId, String resumeCode,
                                   String description);

  String generateDecontrolNlrLetter(String userId, String sessionId, String stageId, String resumeCode,
                                    String description);
}