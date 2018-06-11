package components.services;

public interface SessionOutcomeService {
  void generateNotFoundNlrLetter(String sessionId, String controlEntryId, String resumeCode);

  void generateDecontrolNlrLetter(String sessionId, String stageId, String resumeCode);
}
