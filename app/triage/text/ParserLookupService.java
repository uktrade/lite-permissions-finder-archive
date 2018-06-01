package triage.text;

import models.cms.ControlEntry;
import models.cms.GlobalDefinition;
import models.cms.LocalDefinition;

import java.util.Optional;

public interface ParserLookupService {

  Optional<ControlEntry> getControlEntryForCode(String code);

  Optional<GlobalDefinition> getGlobalDefinitionForTerm(String term);

  Optional<LocalDefinition> getLocalDefinitionForTerm(String term, String controlEntryId);

}
