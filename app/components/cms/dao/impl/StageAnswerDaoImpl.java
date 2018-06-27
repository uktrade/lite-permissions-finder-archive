package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.StageAnswerDao;
import components.cms.jdbi.StageAnswerJDBIDao;
import models.cms.StageAnswer;
import models.cms.enums.OutcomeType;
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
  public List<StageAnswer> getStageAnswersByControlEntryIdAndOutcomeType(long controlEntryId, OutcomeType outcomeType) {
    return stageAnswerJDBIDao.getStageAnswersByControlEntryIdAndOutcomeType(controlEntryId, outcomeType.toString());
  }

  @Override
  public List<StageAnswer> getStageAnswersForStageId(long stageId) {
    return stageAnswerJDBIDao.getForStageId(stageId);
  }

  @Override
  public Long insertStageAnswer(StageAnswer stageAnswer) {
    return stageAnswerJDBIDao.insert(
        stageAnswer.getStageId(),
        stageAnswer.getGoToStageId(),
        stageAnswer.getGoToOutcomeType(),
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
