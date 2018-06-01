package triage.cache;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;

public class CachePopulationServiceImpl implements CachePopulationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CachePopulationServiceImpl.class);

  private final JourneyConfigService journeyConfigService;
  private final JourneyConfigCache journeyConfigCache;
  private final ControlEntryDao controlEntryDao;
  private final CacheValidator cacheValidator;

  @Inject
  public CachePopulationServiceImpl(JourneyConfigService journeyConfigService, JourneyConfigCache journeyConfigCache,
                                    ControlEntryDao controlEntryDao, CacheValidator cacheValidator) {
    this.journeyConfigService = journeyConfigService;
    this.journeyConfigCache = journeyConfigCache;
    this.controlEntryDao = controlEntryDao;
    this.cacheValidator = cacheValidator;
  }

  @Override
  public String populateCache() {
    journeyConfigCache.flushCache();
    cacheValidator.reset();

    LOGGER.info("Start config cache population");

    populateCachesForControlEntries();

    populateCachesForStage(journeyConfigService.getStageConfigById(journeyConfigService.getInitialStageId()));

    //TODO: definitions

    String result = String.format("Config cache successfully populated\n" +
            "Bad control entries: %s\n" +
            "Bad global definition terms: %s\n" +
            "Bad local definition terms: %s\n",
        cacheValidator.getUnmatchedControlCodes(), cacheValidator.getUnmatchedGlobalDefinitions(),
        cacheValidator.getUnmatchedLocalDefinitions());

    LOGGER.info(result);

    return result;
  }

  private void populateCachesForStage(StageConfig stageConfig) {
    LOGGER.debug("Populating caches for stage {}", stageConfig.getStageId());

    journeyConfigService.getNoteConfigsByStageId(stageConfig.getStageId());

    stageConfig.getAnswerConfigs().stream()
        .filter(e -> e.getNextStageId().isPresent())
        .map(e -> e.getNextStageId().get())
        .map(journeyConfigService::getStageConfigById)
        .forEach(this::populateCachesForStage);

    if (stageConfig.getNextStageId().isPresent()) {
      populateCachesForStage(journeyConfigService.getStageConfigById(stageConfig.getNextStageId().get()));
    }
  }

  private void populateCachesForControlEntries() {
    controlEntryDao.getAllControlEntries()
        .forEach(e -> journeyConfigService.getControlEntryConfigById(e.getId().toString()));
  }
}
