package utils;

import models.cms.enums.AnswerType;
import models.cms.enums.QuestionType;
import models.enums.PageType;
import triage.config.StageConfig;

public class PageTypeUtil {

  public static PageType getPageType(StageConfig stageConfig) {
    if (stageConfig.getQuestionType() == QuestionType.STANDARD && stageConfig.getAnswerType() == AnswerType.SELECT_ONE) {
      return PageType.SELECT_ONE;
    } else if (stageConfig.getQuestionType() == QuestionType.STANDARD && stageConfig.getAnswerType() == AnswerType.SELECT_MANY) {
      return PageType.SELECT_MANY;
    } else if (stageConfig.getQuestionType() == QuestionType.DECONTROL && stageConfig.getAnswerType() == AnswerType.SELECT_MANY) {
      return PageType.DECONTROL;
    } else if (stageConfig.getQuestionType() == QuestionType.ITEM) {
      return PageType.ITEM;
    } else {
      return PageType.UNKNOWN;
    }
  }

}
