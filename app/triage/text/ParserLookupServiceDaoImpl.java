package triage.text;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.GlobalDefinitionDao;
import models.cms.ControlEntry;
import models.cms.GlobalDefinition;
import models.cms.LocalDefinition;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ParserLookupServiceDaoImpl implements ParserLookupService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ParserLookupServiceDaoImpl.class);

  private final ControlEntryDao controlEntryDao;
  private final GlobalDefinitionDao globalDefinitionDao;

  @Inject
  public ParserLookupServiceDaoImpl(ControlEntryDao controlEntryDao, GlobalDefinitionDao globalDefinitionDao) {
    this.controlEntryDao = controlEntryDao;
    this.globalDefinitionDao = globalDefinitionDao;
  }

  @Override
  public Optional<ControlEntry> getControlEntryForCode(String code) {
    ControlEntry controlEntry = controlEntryDao.getControlEntryByControlCode(code);
    if (controlEntry == null) {
      LOGGER.warn("Code {} not matched", code);
    }

    return Optional.ofNullable(controlEntry);
  }

  @Override
  public Optional<GlobalDefinition> getGlobalDefinitionForTerm(String term) {
    GlobalDefinition globalDefinition = globalDefinitionDao.getGlobalDefinitionByTerm(term.toLowerCase());
    if (globalDefinition == null) {
      LOGGER.warn("Global definition term '{}' not matched", term);
    }

    return Optional.ofNullable(globalDefinition);
  }

  @Override
  public Optional<LocalDefinition> getLocalDefinitionForTerm(String term, String stageId) {
    return Optional.empty();
  }
}
