package components.services;

import models.AccountData;
import play.twirl.api.Html;
import triage.config.ControlEntryConfig;
import triage.config.StageConfig;

public interface SessionOutcomeService {

  void generateItemListedOutcome(String sessionId, String userId, AccountData accountData,
                                 ControlEntryConfig controlEntryConfig);

  String generateNotFoundNlrLetter(String sessionId, String userId, AccountData accountData,
                                   ControlEntryConfig controlEntryConfig, String resumeCode, Html description);

  String generateDecontrolNlrLetter(String sessionId, String userId, AccountData accountData,
                                    StageConfig stageConfig, String resumeCode, Html description);
}
