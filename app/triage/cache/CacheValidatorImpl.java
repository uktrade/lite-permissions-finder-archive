package triage.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CacheValidatorImpl implements CacheValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(CacheValidatorImpl.class);

  private final Set<String> unmatchedControlCodes = new HashSet<>();
  private final Set<String> unmatchedGlobalDefinitions = new HashSet<>();
  private final Set<String> unmatchedLocalDefinitions = new HashSet<>();

  @Override
  public void logUnmatchedControlCode(String controlCode) {
    LOGGER.warn("Code {} not matched", controlCode);
    unmatchedControlCodes.add(controlCode);
  }

  @Override
  public void logUnmatchedGlobalDefinition(String term) {
    LOGGER.warn("Global definition term '{}' not matched", term);
    unmatchedGlobalDefinitions.add(term);
  }

  @Override
  public void logUnmatchedLocalDefinition(String term) {
    unmatchedLocalDefinitions.add(term);
  }

  @Override
  public Collection<String> getUnmatchedControlCodes() {
    return unmatchedControlCodes;
  }

  @Override
  public Collection<String> getUnmatchedGlobalDefinitions() {
    return unmatchedGlobalDefinitions;
  }

  @Override
  public Collection<String> getUnmatchedLocalDefinitions() {
    return unmatchedLocalDefinitions;
  }

  @Override
  public void reset() {
    unmatchedControlCodes.clear();
    unmatchedGlobalDefinitions.clear();
    unmatchedLocalDefinitions.clear();
  }
}
