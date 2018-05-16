package components.cms.dao;

import models.cms.Stage;

public interface StageDao {
  Stage getStage(long id);

  Long insertStage(Stage stage);

  void deleteStage(long id);

  void deleteAllStages();

}
