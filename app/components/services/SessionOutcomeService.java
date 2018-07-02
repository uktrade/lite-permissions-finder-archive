package components.services;

import exceptions.InvalidUserAccountException;
import play.twirl.api.Html;
import triage.config.ControlEntryConfig;
import triage.config.StageConfig;

public interface SessionOutcomeService {

  void generateItemListedOutcome(String userId, String sessionId,
                                 ControlEntryConfig controlEntryConfig) throws InvalidUserAccountException;

  String generateNotFoundNlrLetter(String userId, String sessionId, ControlEntryConfig controlEntryConfig,
                                   String resumeCode, Html description) throws InvalidUserAccountException;

  String generateDecontrolNlrLetter(String userId, String sessionId, StageConfig stageConfig, String resumeCode,
                                    Html description) throws InvalidUserAccountException;
}
