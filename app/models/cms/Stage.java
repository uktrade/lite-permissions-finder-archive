package models.cms;

import models.cms.enums.AnswerType;
import models.cms.enums.QuestionType;

public class Stage {
  private Long id;
  private Long journeyId;
  private Long controlEntryId;
  private String title;
  private String explanatoryNotes;
  private QuestionType questionType;
  private AnswerType answerType;
  private Long nextStageId;

  public Long getId() {
    return id;
  }

  public Stage setId(Long id) {
    this.id = id;
    return this;
  }

  public Long getJourneyId() {
    return journeyId;
  }

  public Stage setJourneyId(Long journeyId) {
    this.journeyId = journeyId;
    return this;
  }

  public Long getControlEntryId() {
    return controlEntryId;
  }

  public Stage setControlEntryId(Long controlEntryId) {
    this.controlEntryId = controlEntryId;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public Stage setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getExplanatoryNotes() {
    return explanatoryNotes;
  }

  public Stage setExplanatoryNotes(String explanatoryNotes) {
    this.explanatoryNotes = explanatoryNotes;
    return this;
  }

  public QuestionType getQuestionType() {
    return questionType;
  }

  public Stage setQuestionType(QuestionType questionType) {
    this.questionType = questionType;
    return this;
  }

  public AnswerType getAnswerType() {
    return answerType;
  }

  public Stage setAnswerType(AnswerType answerType) {
    this.answerType = answerType;
    return this;
  }

  public Long getNextStageId() {
    return nextStageId;
  }

  public Stage setNextStageId(Long nextStageId) {
    this.nextStageId = nextStageId;
    return this;
  }
}
