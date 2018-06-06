package triage.config;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.JourneyDao;
import components.cms.dao.StageAnswerDao;
import components.cms.dao.StageDao;
import models.cms.Journey;
import models.cms.Stage;
import models.cms.StageAnswer;
import models.cms.enums.StageAnswerOutcomeType;
import triage.cache.JourneyConfigCache;

import java.util.List;
import java.util.stream.Collectors;

public class JourneyConfigServiceImpl implements JourneyConfigService {

  private final JourneyDao journeyDao;
  private final StageDao stageDao;
  private final StageAnswerDao stageAnswerDao;
  private final ControlEntryDao controlEntryDao;
  private final JourneyConfigCache journeyConfigCache;

  @Inject
  public JourneyConfigServiceImpl(JourneyDao journeyDao, StageDao stageDao, StageAnswerDao stageAnswerDao,
                                  ControlEntryDao controlEntryDao, JourneyConfigCache journeyConfigCache) {
    this.journeyDao = journeyDao;
    this.stageDao = stageDao;
    this.stageAnswerDao = stageAnswerDao;
    this.controlEntryDao = controlEntryDao;
    this.journeyConfigCache = journeyConfigCache;
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
    return journeyConfigCache.getStageConfigById(stageId);
  }

  @Override
  public List<StageAnswer> getStageAnswersByControlEntryIdAndOutcomeType(long controlEntryId,
                                                                         StageAnswerOutcomeType stageAnswerOutcomeType) {
    return stageAnswerDao.getStageAnswersByControlEntryIdAndOutcomeType(controlEntryId, stageAnswerOutcomeType);
  }

  @Override
  public AnswerConfig getStageAnswerForPreviousStage(String stageId) {
    StageAnswer stageAnswer = stageAnswerDao.getStageAnswerByGoToStageId(Long.parseLong(stageId));
    if (stageAnswer != null) {
      return journeyConfigCache.getStageConfigById(stageAnswer.getParentStageId().toString())
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
    return journeyConfigCache.getNoteConfigsByStageId(stageId);
  }

  @Override
  public ControlEntryConfig getControlEntryConfigById(String controlEntryId) {
    return journeyConfigCache.getControlEntryConfigById(controlEntryId);
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
}
