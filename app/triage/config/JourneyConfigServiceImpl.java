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
import models.cms.enums.QuestionType;
import triage.cache.JourneyConfigFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JourneyConfigServiceImpl implements JourneyConfigService {

  private final JourneyDao journeyDao;
  private final StageDao stageDao;
  private final StageAnswerDao stageAnswerDao;
  private final ControlEntryDao controlEntryDao;

  private final LoadingCache<String, Optional<StageConfig>> stageConfigCache;
  private final LoadingCache<String, Optional<ControlEntryConfig>> controlEntryCache;
  private final LoadingCache<String, List<ControlEntryConfig>> relatedControlEntryCache;
  private final LoadingCache<String, List<NoteConfig>> noteCache;
  private final LoadingCache<Long, Journey> journeyCache;

  @Inject
  public JourneyConfigServiceImpl(JourneyDao journeyDao, StageDao stageDao, StageAnswerDao stageAnswerDao,
                                  ControlEntryDao controlEntryDao, JourneyConfigFactory journeyConfigFactory) {
    this.journeyDao = journeyDao;
    this.stageDao = stageDao;
    this.stageAnswerDao = stageAnswerDao;
    this.controlEntryDao = controlEntryDao;
    this.stageConfigCache = CacheBuilder.newBuilder().build(CacheLoader.from(journeyConfigFactory::createStageConfigForId));
    this.controlEntryCache = CacheBuilder.newBuilder().build(CacheLoader.from(journeyConfigFactory::createControlEntryConfigForId));
    this.relatedControlEntryCache = CacheBuilder.newBuilder().build(CacheLoader.from(journeyConfigFactory::createRelatedControlEntryConfigsForId));
    this.noteCache = CacheBuilder.newBuilder().build(CacheLoader.from(journeyConfigFactory::createNoteConfigsForStageId));
    this.journeyCache = CacheBuilder.newBuilder().build(CacheLoader.from(journeyId -> journeyDao.getJourney(journeyId)));
  }

  @Override
  public Optional<StageConfig> getStageConfigById(String stageId) {
    return stageConfigCache.getUnchecked(stageId);
  }

  @Override
  public String getJourneyNameByJourneyId(long journeyId) {
    return journeyCache.getUnchecked(journeyId).getJourneyName();
  }

  @Override
  public StageConfig getStageConfigForInitialJourneyStage(long journeyId) {
    return stageConfigCache.getUnchecked(Long.toString(journeyCache.getUnchecked(journeyId).getInitialStageId())).get();
  }

  @Override
  public List<String> getInitialStagesForAllJourneys() {
    return journeyDao.getAllJourneys()
      .stream()
      .map(Journey::getInitialStageId)
      .map(stageId -> stageId.toString())
      .collect(Collectors.toList());
  }

  @Override
  public AnswerConfig getStageAnswerForPreviousStage(String stageId) {
    StageAnswer stageAnswer = stageAnswerDao.getStageAnswerByGoToStageId(Long.parseLong(stageId));
    if (stageAnswer != null) {
      Optional<StageConfig> stageConfigOptional = stageConfigCache.getUnchecked(stageAnswer.getStageId().toString());
      return stageConfigOptional.map(stageConfig -> stageConfig
              .getAnswerConfigs()
              .stream()
              .filter(e -> e.getAnswerId().equals(stageAnswer.getId().toString()))
              .findFirst()
              .orElse(null)).orElse(null);
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
        return getStageConfigById(stageAnswer.getStageId().toString()).orElse(null);
      } else {
        return null;
      }
    } else {
      return getStageConfigById(stage.getId().toString()).orElse(null);
    }
  }

  @Override
  public List<NoteConfig> getNoteConfigsByStageId(String stageId) {
    return noteCache.getUnchecked(stageId);
  }

  @Override
  public Optional<ControlEntryConfig> getControlEntryConfigById(String controlEntryId) {
    return controlEntryCache.getUnchecked(controlEntryId);
  }

  @Override
  public List<String> getStageIdsForControlEntryId(String controlEntryId) {
    return stageDao.getStagesForControlEntryId(Long.valueOf(controlEntryId))
        .stream()
        .map(Stage::getId)
        .map(id -> id.toString())
        .collect(Collectors.toList());
  }

  @Override
  public List<ControlEntryConfig> getChildRatings(ControlEntryConfig controlEntryConfig) {
    return controlEntryDao
        .getChildControlEntries(Long.parseLong(controlEntryConfig.getId()))
        .stream()
        .map(e -> e.getId().toString())
        .map(this::getControlEntryConfigById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<StageConfig> getPrincipleStageConfigForControlEntry(ControlEntryConfig controlEntryConfig) {
    return getStageIdsForControlEntryId(controlEntryConfig.getId()).stream()
        .map(this::getStageConfigById)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(stageConfig -> stageConfig.getQuestionType() == QuestionType.STANDARD ||
            stageConfig.getQuestionType() == QuestionType.ITEM)
        .findAny();
  }

  @Override
  public List<ControlEntryConfig> getRelatedControlEntries(ControlEntryConfig controlEntryConfig) {
    return relatedControlEntryCache.getUnchecked(controlEntryConfig.getId());
  }

  @Override
  public void flushCache() {
    stageConfigCache.invalidateAll();
    controlEntryCache.invalidateAll();
    relatedControlEntryCache.invalidateAll();
    noteCache.invalidateAll();

    stageConfigCache.cleanUp();
    controlEntryCache.cleanUp();
    noteCache.cleanUp();
  }
}
