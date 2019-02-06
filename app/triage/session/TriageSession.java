package triage.session;

import java.util.Set;
import lombok.Data;

@Data
public class TriageSession {

  private final String id;
  private final Long journeyId;
  private final String resumeCode;
  private final long spreadsheetVersionId;
  private final Long lastStageId;
  private final Set<String> decontrolledCodesFound;
  private final Set<String> controlEntryIdsToVerifyDecontrolledStatus;
}
