package triage.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.JourneyDao;
import components.cms.dao.StageAnswerDao;
import components.cms.dao.StageDao;
import models.cms.Journey;
import models.cms.Stage;
import models.cms.StageAnswer;
import models.cms.enums.StageAnswerOutcomeType;
import triage.cache.JourneyConfigFactory;

import java.util.List;
import java.util.stream.Collectors;

public class JourneyConfigServiceImpl implements JourneyConfigService {

  private final JourneyDao journeyDao;
  private final StageDao stageDao;
  private final StageAnswerDao stageAnswerDao;
  private final ControlEntryDao controlEntryDao;

  private final LoadingCache<String, StageConfig> stageConfigCache;
  private final LoadingCache<String, ControlEntryConfig> controlEntryCache;
  private final LoadingCache<String, List<NoteConfig>> noteCache;


  @Inject
  public JourneyConfigServiceImpl(JourneyDao journeyDao, StageDao stageDao, StageAnswerDao stageAnswerDao,
                                  ControlEntryDao controlEntryDao, JourneyConfigFactory journeyConfigFactory) {
    this.journeyDao = journeyDao;
    this.stageDao = stageDao;
    this.stageAnswerDao = stageAnswerDao;
    this.controlEntryDao = controlEntryDao;
    this.stageConfigCache = CacheBuilder.newBuilder().build(CacheLoader.from(journeyConfigFactory::createStageConfigForId));
    this.controlEntryCache = CacheBuilder.newBuilder().build(CacheLoader.from(journeyConfigFactory::createControlEntryConfigForId));
    this.noteCache = CacheBuilder.newBuilder().build(CacheLoader.from(journeyConfigFactory::createNoteConfigsForStageId));
  }

  @Override
  public String getInitialStageId() {
    return journeyDao.getJourneysByJourneyName("MILITARY").stream()
        .map(Journey::getInitialStageId)
        .map(Object::toString)
        .findFirst()
        .orElse(null);
  }

  @Override
  public StageConfig getStageConfigById(String stageId) {
    return stageConfigCache.getUnchecked(stageId);
  }

  @Override
  public List<StageConfig> getStageConfigsByControlEntryIdAndOutcomeType(long controlEntryId,
                                                                         StageAnswerOutcomeType stageAnswerOutcomeType) {
    return stageAnswerDao.getStageAnswersByControlEntryIdAndOutcomeType(controlEntryId, stageAnswerOutcomeType).stream()
        .map(StageAnswer::getParentStageId)
        .distinct()
        .map(stageId -> stageConfigCache.getUnchecked(Long.toString(stageId)))
        .collect(Collectors.toList());
  }

  @Override
  public AnswerConfig getStageAnswerForPreviousStage(String stageId) {
    StageAnswer stageAnswer = stageAnswerDao.getStageAnswerByGoToStageId(Long.parseLong(stageId));
    if (stageAnswer != null) {
      return stageConfigCache.getUnchecked(stageAnswer.getParentStageId().toString())
          .getAnswerConfigs()
          .stream()
          .filter(e -> e.getAnswerId().equals(stageAnswer.getId().toString()))
          .findFirst()
          .orElse(null);
    } else {
      return null;
    }
  }

  @Override
  public StageConfig getStageConfigForPreviousStage(String stageId) {
    Stage stage = stageDao.getByNextStageId(Long.parseLong(stageId));
    if (stage == null) {
      StageAnswer stageAnswer = stageAnswerDao.getStageAnswerByGoToStageId(Long.parseLong(stageId));
      if (stageAnswer != null) {
        return getStageConfigById(stageAnswer.getParentStageId().toString());
      } else {
        return null;
      }
    } else {
      return getStageConfigById(stage.getId().toString());
    }
  }

  @Override
  public List<NoteConfig> getNoteConfigsByStageId(String stageId) {
    return noteCache.getUnchecked(stageId);
  }

  @Override
  public ControlEntryConfig getControlEntryConfigById(String controlEntryId) {
    return controlEntryCache.getUnchecked(controlEntryId);
  }

  @Override
  public List<String> getStageIdsForControlEntry(ControlEntryConfig controlEntryConfig) {
    return stageDao.getStagesForControlEntryId(Long.parseLong(controlEntryConfig.getId()))
        .stream()
        .map(e -> e.getId().toString())
        .collect(Collectors.toList());
  }

  @Override
  public List<ControlEntryConfig> getChildRatings(ControlEntryConfig controlEntryConfig) {
    return controlEntryDao
        .getChildControlEntries(Long.parseLong(controlEntryConfig.getId()))
        .stream()
        .map(e -> e.getId().toString())
        .map(this::getControlEntryConfigById)
        .collect(Collectors.toList());
  }

  @Override
  public void flushCache() {
    stageConfigCache.invalidateAll();
    controlEntryCache.invalidateAll();
    noteCache.invalidateAll();

    stageConfigCache.cleanUp();
    controlEntryCache.cleanUp();
    noteCache.cleanUp();
  }

}
