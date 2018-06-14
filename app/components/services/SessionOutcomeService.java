package components.services;

import exceptions.InvalidUserAccountException;

public interface SessionOutcomeService {

  void generateItemListedOutcome(String userId, String sessionId, String controlEntryId) throws InvalidUserAccountException;

  String generateNotFoundNlrLetter(String userId, String sessionId, String controlEntryId, String resumeCode,
                                   String description) throws InvalidUserAccountException;

  String generateDecontrolNlrLetter(String userId, String sessionId, String stageId, String resumeCode,
                                    String description) throws InvalidUserAccountException;
}
