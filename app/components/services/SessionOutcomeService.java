package components.services;

import exceptions.TooManyCustomersOrSitesException;

public interface SessionOutcomeService {

  void generateItemListedOutcome(String userId, String sessionId, String controlEntryId) throws TooManyCustomersOrSitesException;

  String generateNotFoundNlrLetter(String userId, String sessionId, String controlEntryId, String resumeCode,
                                   String description) throws TooManyCustomersOrSitesException;

  String generateDecontrolNlrLetter(String userId, String sessionId, String stageId, String resumeCode,
                                    String description) throws TooManyCustomersOrSitesException;
}
