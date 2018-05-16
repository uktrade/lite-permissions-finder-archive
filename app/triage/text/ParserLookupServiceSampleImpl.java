package triage.text;

import com.google.inject.Inject;
import triage.config.ControlEntryConfig;
import triage.config.DefinitionConfig;

import java.util.Optional;

public class ParserLookupServiceSampleImpl implements ParserLookupService {

  @Inject
  public ParserLookupServiceSampleImpl() {
  }

  @Override
  public Optional<ControlEntryConfig> getControlEntryForCode(String code) {
    return Optional.empty();
  }

  @Override
  public Optional<DefinitionConfig> getGlobalDefinitionForTerm(String term) {
    return Optional.empty();
  }

  @Override
  public Optional<DefinitionConfig> getLocalDefinitionForTerm(String term, String stageId) {
    return Optional.empty();
  }
}
