package triage.cache;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.GlobalDefinitionDao;
import components.cms.dao.LocalDefinitionDao;
import exceptions.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import triage.config.AnswerConfig;
import triage.config.DefinitionConfigService;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;

import java.util.Optional;

public class CachePopulationServiceImpl implements CachePopulationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CachePopulationServiceImpl.class);

  private final JourneyConfigService journeyConfigService;
  private final ControlEntryDao controlEntryDao;
  private final CacheValidator cacheValidator;
  private final DefinitionConfigService definitionConfigService;
  private final GlobalDefinitionDao globalDefinitionDao;
  private final LocalDefinitionDao localDefinitionDao;

  @Inject
  public CachePopulationServiceImpl(JourneyConfigService journeyConfigService, ControlEntryDao controlEntryDao,
                                    CacheValidator cacheValidator,
                                    DefinitionConfigService definitionConfigService,
                                    GlobalDefinitionDao globalDefinitionDao,
                                    LocalDefinitionDao localDefinitionDao) {
    this.journeyConfigService = journeyConfigService;
    this.controlEntryDao = controlEntryDao;
    this.cacheValidator = cacheValidator;
    this.definitionConfigService = definitionConfigService;
    this.globalDefinitionDao = globalDefinitionDao;
    this.localDefinitionDao = localDefinitionDao;
  }

  @Override
  public String populateCache() {
    definitionConfigService.flushCache();
    journeyConfigService.flushCache();

    cacheValidator.reset();

    LOGGER.info("Start config cache population");

    localDefinitionDao.getAllIds().forEach(id -> definitionConfigService.getLocalDefinition(Long.toString(id)));
    globalDefinitionDao.getAllIds().forEach(id -> definitionConfigService.getGlobalDefinition(Long.toString(id)));

    populateCachesForControlEntries();

    String initialStageId = journeyConfigService.getInitialStageId();
    populateCachesForStage(initialStageId);

    String result = String.format("Config cache successfully populated\n" +
            "Bad control entries: %s\n" +
            "Bad global definition terms: %s\n" +
            "Bad local definition terms: %s\n",
        cacheValidator.getUnmatchedControlCodes(), cacheValidator.getUnmatchedGlobalDefinitions(),
        cacheValidator.getUnmatchedLocalDefinitions());

    LOGGER.info(result);

    return result;
  }

  private void populateCachesForStage(String stageId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigById(stageId).orElseThrow(() ->
        new BusinessRuleException("Unknown stageId " + stageId));

    LOGGER.debug("Populating caches for stage {}", stageConfig.getStageId());

    journeyConfigService.getNoteConfigsByStageId(stageConfig.getStageId());

    stageConfig.getAnswerConfigs().stream()
        .map(AnswerConfig::getNextStageId)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(this::populateCachesForStage);

    stageConfig.getNextStageId().ifPresent(this::populateCachesForStage);
  }

  private void populateCachesForControlEntries() {
    controlEntryDao.getAllControlEntries().stream()
        .map(controlEntry -> {
          String controlEntryId = Long.toString(controlEntry.getId());
          return journeyConfigService.getControlEntryConfigById(controlEntryId).orElseThrow(() ->
              new BusinessRuleException("Unknown controlEntryId " + controlEntry.getId()));
        })
        .forEach(journeyConfigService::getRelatedControlEntries);
  }
}
