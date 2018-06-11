package components.services;

public interface SessionOutcomeService {

  String generateNotFoundNlrLetter(String sessionId, String controlEntryId, String resumeCode);

  String generateDecontrolNlrLetter(String sessionId, String stageId, String resumeCode);

}
