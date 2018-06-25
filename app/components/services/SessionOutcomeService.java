package components.services;

import exceptions.InvalidUserAccountException;
import play.twirl.api.Html;

public interface SessionOutcomeService {

  void generateItemListedOutcome(String userId, String sessionId, String controlEntryId) throws InvalidUserAccountException;

  String generateNotFoundNlrLetter(String userId, String sessionId, String controlEntryId, String resumeCode,
                                   Html description) throws InvalidUserAccountException;

  String generateDecontrolNlrLetter(String userId, String sessionId, String stageId, String resumeCode,
                                    Html description) throws InvalidUserAccountException;
}
