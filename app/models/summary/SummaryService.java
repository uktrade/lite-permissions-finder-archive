package models.summary;

import java.util.concurrent.CompletionStage;

public interface SummaryService {
  CompletionStage<Summary> composeSummary();
}
