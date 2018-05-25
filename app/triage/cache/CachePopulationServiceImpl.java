package triage.cache;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;

public class CachePopulationServiceImpl implements CachePopulationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CachePopulationServiceImpl.class);

  private final JourneyConfigService journeyConfigService;
  private final JourneyConfigCache journeyConfigCache;
  private final CacheValidator cacheValidator;

  @Inject
  public CachePopulationServiceImpl(JourneyConfigService journeyConfigService, JourneyConfigCache journeyConfigCache,
                                    CacheValidator cacheValidator) {
    this.journeyConfigService = journeyConfigService;
    this.journeyConfigCache = journeyConfigCache;
    this.cacheValidator = cacheValidator;
  }

  @Override
  public String populateCache() {
    journeyConfigCache.flushCache();
    cacheValidator.reset();

    populateCachesForStage(journeyConfigService.getStageConfigById(journeyConfigService.getInitialStageId()));
    //TODO: definitions

    return String.format("Caches populated\nBad control entries: %s\nBad global definition terms: %s\n",
        cacheValidator.getUnmatchedControlCodes(), cacheValidator.getUnmatchedGlobalDefinitions());
  }


  private void populateCachesForStage(StageConfig stageConfig) {
    LOGGER.info("Populating caches for stage {}", stageConfig.getStageId());

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
}
