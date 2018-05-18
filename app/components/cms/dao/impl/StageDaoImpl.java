package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.StageDao;
import components.cms.jdbi.StageJDBIDao;
import models.cms.Stage;
import org.skife.jdbi.v2.DBI;

import java.util.List;

public class StageDaoImpl implements StageDao {

  private final StageJDBIDao stageJDBIDao;

  @Inject
  public StageDaoImpl(DBI dbi) {
    this.stageJDBIDao = dbi.onDemand(StageJDBIDao.class);
  }

  @Override
  public Stage getStage(long id) {
    return stageJDBIDao.get(id);
  }

  @Override
  public List<Stage> getStagesForControlEntryId(long controlEntryId) {
    return stageJDBIDao.getForControlEntryId(controlEntryId);
  }

  @Override
  public Long insertStage(Stage stage) {
    return stageJDBIDao.insert(
        stage.getJourneyId(),
        stage.getControlEntryId(),
        stage.getTitle(),
        stage.getExplanatoryNotes(),
        stage.getQuestionType(),
        stage.getAnswerType(),
        stage.getNextStageId());
  }

  @Override
  public void deleteStage(long id) {
    stageJDBIDao.delete(id);
  }

  @Override
  public void deleteAllStages() {
    stageJDBIDao.truncate();
  }
}
