package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.StageAnswerDao;
import components.cms.jdbi.StageAnswerJDBIDao;
import models.cms.StageAnswer;
import models.cms.enums.StageAnswerOutcomeType;
import org.skife.jdbi.v2.DBI;

import java.util.List;

public class StageAnswerDaoImpl implements StageAnswerDao {

  private final StageAnswerJDBIDao stageAnswerJDBIDao;

  @Inject
  public StageAnswerDaoImpl(DBI dbi) {
    this.stageAnswerJDBIDao = dbi.onDemand(StageAnswerJDBIDao.class);
  }

  @Override
  public StageAnswer getStageAnswer(long id) {
    return stageAnswerJDBIDao.get(id);
  }

  @Override
  public StageAnswer getStageAnswerByGoToStageId(long goToStageId) {
    return stageAnswerJDBIDao.getStageAnswerByGoToStageId(goToStageId);
  }

  @Override
  public List<StageAnswer> getStageAnswersByControlEntryIdAndOutcomeType(
      long controlEntryId,
      StageAnswerOutcomeType stageAnswerOutcomeType) {
    return stageAnswerJDBIDao.getStageAnswersByControlEntryIdAndOutcomeType(controlEntryId, stageAnswerOutcomeType.toString());
  }

  @Override
  public List<StageAnswer> getStageAnswersForStageId(long stageId) {
    return stageAnswerJDBIDao.getForParentStageId(stageId);
  }

  @Override
  public Long insertStageAnswer(StageAnswer stageAnswer) {
    return stageAnswerJDBIDao.insert(
        stageAnswer.getParentStageId(),
        stageAnswer.getGoToStageId(),
        stageAnswer.getGoToStageAnswerOutcomeType(),
        stageAnswer.getControlEntryId(),
        stageAnswer.getAnswerText(),
        stageAnswer.getDisplayOrder(),
        stageAnswer.getAnswerPrecedence(),
        stageAnswer.isDividerAbove(),
        stageAnswer.getNestedContent(),
        stageAnswer.getMoreInfoContent());
  }

  @Override
  public void deleteStageAnswer(long id) {
    stageAnswerJDBIDao.delete(id);
  }

  @Override
  public void deleteAllStageAnswers() {
    stageAnswerJDBIDao.truncate();
  }
}
