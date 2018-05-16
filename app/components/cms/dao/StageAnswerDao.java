package components.cms.dao;

import models.cms.StageAnswer;

public interface StageAnswerDao {

  StageAnswer getStageAnswer(long id);

  Long insertStageAnswer(StageAnswer stageAnswer);

  void deleteStageAnswer(long id);

  void deleteAllStageAnswers();

}
