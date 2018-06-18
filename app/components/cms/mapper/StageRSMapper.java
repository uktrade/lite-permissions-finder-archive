package components.cms.mapper;

import components.cms.mapper.util.ResultSetWrapper;
import models.cms.Stage;
import models.cms.enums.AnswerType;
import models.cms.enums.QuestionType;
import models.cms.enums.StageOutcomeType;
import org.apache.commons.lang3.EnumUtils;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StageRSMapper implements ResultSetMapper<Stage> {
  @Override
  public Stage map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    ResultSetWrapper rsw = new ResultSetWrapper(r);
    Long id = rsw.getLong("id");
    Long journeyId = rsw.getLong("journey_id");
    Long controlEntryId = rsw.getLong("control_entry_id");
    String title = r.getString("title");
    String explanatoryNotes = r.getString("explanatory_notes");
    QuestionType questionType = EnumUtils.getEnum(QuestionType.class, r.getString("question_type"));
    AnswerType answerType = EnumUtils.getEnum(AnswerType.class, r.getString("answer_type"));
    Long nextStageId = rsw.getLong("next_stage_id");
    String stageOutcomeTypeStr = r.getString("go_to_stage_outcome_type");
    StageOutcomeType stageOutcomeType = EnumUtils.getEnum(StageOutcomeType.class, stageOutcomeTypeStr);
    return new Stage()
        .setId(id)
        .setJourneyId(journeyId)
        .setControlEntryId(controlEntryId)
        .setTitle(title)
        .setExplanatoryNotes(explanatoryNotes)
        .setQuestionType(questionType)
        .setAnswerType(answerType)
        .setNextStageId(nextStageId)
        .setStageOutcomeType(stageOutcomeType);
  }
}
