package components.services;

public interface SessionOutcomeService {

  String generateNotFoundNlrLetter(String userId, String sessionId, String controlEntryId, String resumeCode);

  String generateDecontrolNlrLetter(String userId, String sessionId, String stageId, String resumeCode);
}
