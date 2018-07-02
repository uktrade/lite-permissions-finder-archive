package triage.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.JourneyDao;
import components.cms.dao.StageAnswerDao;
import components.cms.dao.StageDao;
import exceptions.UnknownParameterException;
import models.cms.Journey;
import models.cms.Stage;
import models.cms.StageAnswer;
import models.cms.enums.QuestionType;
import triage.cache.JourneyConfigFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JourneyConfigServiceImpl implements JourneyConfigService {

  public static final String DEFAULT_JOURNEY_NAME = "MILITARY";

  private final JourneyDao journeyDao;
  private final StageDao stageDao;
  private final StageAnswerDao stageAnswerDao;
  private final ControlEntryDao controlEntryDao;

  private final LoadingCache<String, Optional<StageConfig>> stageConfigCache;
  private final LoadingCache<String, Optional<ControlEntryConfig>> controlEntryCache;
  private final LoadingCache<String, List<ControlEntryConfig>> relatedControlEntryCache;
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
    this.relatedControlEntryCache = CacheBuilder.newBuilder().build(CacheLoader.from(journeyConfigFactory::createRelatedControlEntryConfigsForId));
    this.noteCache = CacheBuilder.newBuilder().build(CacheLoader.from(journeyConfigFactory::createNoteConfigsForStageId));
  }

  @Override
  public long getDefaultJourneyId() {
    return getDefaultJourney()
        .map(Journey::getId)
        .orElseThrow(() -> new IllegalStateException("No default journey is defined"));
  }

  @Override
  public String getInitialStageId() {
    return getDefaultJourney()
        .map(Journey::getInitialStageId)
        .map(Object::toString)
        .orElseThrow(() -> new IllegalStateException("No default journey is defined"));
  }

  private Optional<Journey> getDefaultJourney() {
    return journeyDao.getJourneysByJourneyName(DEFAULT_JOURNEY_NAME).stream().findAny();
  }

  @Override
  public StageConfig getStageConfigNotNull(String stageId) {
    return stageConfigCache.getUnchecked(stageId)
        .orElseThrow(() -> new UnknownParameterException("Unknown stageId " + stageId));
  }

  @Override
  public StageConfig getStageConfigById(String stageId) {
    return stageConfigCache.getUnchecked(stageId).orElse(null);
  }

  @Override
  public AnswerConfig getStageAnswerForPreviousStage(String stageId) {
    StageAnswer stageAnswer = stageAnswerDao.getStageAnswerByGoToStageId(Long.parseLong(stageId));
    if (stageAnswer != null) {
      Optional<StageConfig> stageConfigOptional = stageConfigCache.getUnchecked(stageAnswer.getStageId().toString());
      if (stageConfigOptional.isPresent()) {
        return stageConfigOptional.get()
            .getAnswerConfigs()
            .stream()
            .filter(e -> e.getAnswerId().equals(stageAnswer.getId().toString()))
            .findFirst()
            .orElse(null);
      } else {
        return null;
      }
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
        return getStageConfigById(stageAnswer.getStageId().toString());
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
  public ControlEntryConfig getControlEntryNotNull(String controlEntryId) {
    return controlEntryCache.getUnchecked(controlEntryId)
        .orElseThrow(() -> new UnknownParameterException("Unknown controlEntryId " + controlEntryId));
  }

  @Override
  public ControlEntryConfig getControlEntryConfigById(String controlEntryId) {
    return controlEntryCache.getUnchecked(controlEntryId).orElse(null);
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
  public Optional<StageConfig> getPrincipleStageConfigForControlEntry(ControlEntryConfig controlEntryConfig) {
    return getStageIdsForControlEntry(controlEntryConfig).stream()
        .map(this::getStageConfigById)
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
