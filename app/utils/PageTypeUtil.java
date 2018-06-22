package utils;

import models.enums.PageType;
import triage.config.StageConfig;

public class PageTypeUtil {

  public static PageType getPageType(StageConfig stageConfig) {
    if (stageConfig.getQuestionType() == StageConfig.QuestionType.STANDARD && stageConfig.getAnswerType() == StageConfig.AnswerType.SELECT_ONE) {
      return PageType.SELECT_ONE;
    } else if (stageConfig.getQuestionType() == StageConfig.QuestionType.STANDARD && stageConfig.getAnswerType() == StageConfig.AnswerType.SELECT_MANY) {
      return PageType.SELECT_MANY;
    } else if (stageConfig.getQuestionType() == StageConfig.QuestionType.DECONTROL && stageConfig.getAnswerType() == StageConfig.AnswerType.SELECT_MANY) {
      return PageType.DECONTROL;
    } else if (stageConfig.getQuestionType() == StageConfig.QuestionType.ITEM) {
      return PageType.ITEM;
    } else {
      return PageType.UNKNOWN;
    }
  }

}
