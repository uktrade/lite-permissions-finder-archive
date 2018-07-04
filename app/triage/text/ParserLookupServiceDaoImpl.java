package triage.text;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.GlobalDefinitionDao;
import components.cms.dao.LocalDefinitionDao;
import models.cms.ControlEntry;
import models.cms.GlobalDefinition;
import models.cms.LocalDefinition;
import triage.cache.CacheValidator;

import java.util.Optional;

public class ParserLookupServiceDaoImpl implements ParserLookupService {

  private final ControlEntryDao controlEntryDao;
  private final GlobalDefinitionDao globalDefinitionDao;
  private final LocalDefinitionDao localDefinitionDao;
  private final CacheValidator cacheValidator;

  @Inject
  public ParserLookupServiceDaoImpl(ControlEntryDao controlEntryDao, GlobalDefinitionDao globalDefinitionDao,
                                    LocalDefinitionDao localDefinitionDao, CacheValidator cacheValidator) {
    this.controlEntryDao = controlEntryDao;
    this.globalDefinitionDao = globalDefinitionDao;
    this.localDefinitionDao = localDefinitionDao;
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
  public Optional<GlobalDefinition> getGlobalDefinitionForTerm(String term, String journeyId) {
    GlobalDefinition globalDefinition = globalDefinitionDao.getGlobalDefinitionByTermAndJourneyId(term, Long.valueOf(journeyId));
    if (globalDefinition == null) {
      cacheValidator.logUnmatchedGlobalDefinition(term);
    }

    return Optional.ofNullable(globalDefinition);
  }

  @Override
  public Optional<LocalDefinition> getLocalDefinitionForTerm(String term, String controlEntryId) {
    LocalDefinition localDefinition = localDefinitionDao.getLocalDefinitionByTerm(term, Long.parseLong(controlEntryId));
    if (localDefinition == null) {
      cacheValidator.logUnmatchedLocalDefinition(term);
    }

    return Optional.ofNullable(localDefinition);
  }
}
