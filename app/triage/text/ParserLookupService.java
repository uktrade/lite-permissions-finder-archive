package triage.text;

import triage.config.ControlEntryConfig;
import triage.config.DefinitionConfig;

import java.util.Optional;

public interface ParserLookupService {

  Optional<ControlEntryConfig> getControlEntryForCode(String code);

  Optional<DefinitionConfig> getGlobalDefinitionForTerm(String term);

  Optional<DefinitionConfig> getLocalDefinitionForTerm(String term, String stageId);

}
