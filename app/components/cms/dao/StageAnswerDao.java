package components.cms.dao;

import models.cms.StageAnswer;

import java.util.List;

public interface StageAnswerDao {

  StageAnswer getStageAnswerByGoToStageId(long goToStageId);

  List<StageAnswer> getStageAnswersForStageId(long stageId);

  Long insertStageAnswer(StageAnswer stageAnswer);

  void deleteAllStageAnswers();

}
