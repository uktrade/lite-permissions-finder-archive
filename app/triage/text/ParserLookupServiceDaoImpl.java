package triage.text;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.GlobalDefinitionDao;
import models.cms.ControlEntry;
import models.cms.GlobalDefinition;
import models.cms.LocalDefinition;
import org.slf4j.LoggerFactory;
import triage.cache.CacheValidator;

import java.util.Optional;

public class ParserLookupServiceDaoImpl implements ParserLookupService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ParserLookupServiceDaoImpl.class);

  private final ControlEntryDao controlEntryDao;
  private final GlobalDefinitionDao globalDefinitionDao;
  private final CacheValidator cacheValidator;

  @Inject
  public ParserLookupServiceDaoImpl(ControlEntryDao controlEntryDao, GlobalDefinitionDao globalDefinitionDao,
                                    CacheValidator cacheValidator) {
    this.controlEntryDao = controlEntryDao;
    this.globalDefinitionDao = globalDefinitionDao;
    this.cacheValidator = cacheValidator;
  }

  @Override
  public Optional<ControlEntry> getControlEntryForCode(String code) {
    ControlEntry controlEntry = controlEntryDao.getControlEntryByControlCode(code);
    if (controlEntry == null) {
      cacheValidator.logUnmatchedControlCode(code);
    }

    return Optional.ofNullable(controlEntry);
  }

  @Override
  public Optional<GlobalDefinition> getGlobalDefinitionForTerm(String term) {
    GlobalDefinition globalDefinition = globalDefinitionDao.getGlobalDefinitionByTerm(term.toLowerCase());
    if (globalDefinition == null) {
      cacheValidator.logUnmatchedGlobalDefinition(term);
    }

    return Optional.ofNullable(globalDefinition);
  }

  @Override
  public Optional<LocalDefinition> getLocalDefinitionForTerm(String term, String stageId) {
    return Optional.empty();
  }
}
