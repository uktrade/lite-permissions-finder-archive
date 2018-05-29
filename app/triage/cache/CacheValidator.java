package triage.cache;

import java.util.Collection;

public interface CacheValidator {

  void logUnmatchedControlCode(String controlCode);

  void logUnmatchedGlobalDefinition(String term);

  void logUnmatchedLocalDefinition(String term);

  Collection<String> getUnmatchedControlCodes();

  Collection<String> getUnmatchedGlobalDefinitions();

  Collection<String> getUnmatchedLocalDefinitions();

  void reset();

}
