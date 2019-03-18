package components.cms.dao;

import java.util.List;
import models.cms.Stage;

public interface StageDao {

  Stage getStage(long id);

  Stage getByNextStageId(long nextStageId);

  List<Stage> getStagesForControlEntryId(long controlEntryId);

  Long insertStage(Stage stage);

  void deleteAllStages();
}
