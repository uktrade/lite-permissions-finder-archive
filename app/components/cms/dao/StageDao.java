package components.cms.dao;

import models.cms.Stage;

import java.util.List;

public interface StageDao {
  Stage getStage(long id);

  Stage getByNextStageId(long nextStageId);

  List<Stage> getStagesForControlEntryId(long controlEntryId);

  Long insertStage(Stage stage);

  void deleteAllStages();

}
