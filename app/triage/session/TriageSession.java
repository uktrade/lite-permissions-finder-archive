package triage.session;

import lombok.Data;

@Data
public class TriageSession {
  private final String id;
  private final long journeyId;
  private final String resumeCode;
  private final long spreadsheetVersionId;
  private final Long lastStageId;
}
