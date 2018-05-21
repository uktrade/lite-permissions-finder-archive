package components.cms.mapper;

import components.cms.mapper.util.ResultSetWrapper;
import models.cms.StageAnswer;
import models.cms.enums.StageAnswerOutcomeType;
import org.apache.commons.lang3.EnumUtils;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StageAnswerRSMapper implements ResultSetMapper<StageAnswer> {
  @Override
  public StageAnswer map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    ResultSetWrapper rsw = new ResultSetWrapper(r);
    Long id = rsw.getLong("id");
    Long parentStageId = rsw.getLong("parent_stage_id");
    Long goToStageId = rsw.getLong("go_to_stage_id");
    String goToStageAnswerOutcomeTypeStr = rsw.getString("go_to_stage_answer_outcome_type");
    StageAnswerOutcomeType goToStageAnswerOutcomeType = EnumUtils.getEnum(StageAnswerOutcomeType.class, goToStageAnswerOutcomeTypeStr);
    Long controlEntryId = rsw.getLong("control_entry_id");
    String answerText = rsw.getString("answer_text");
    Integer displayOrder = rsw.getInt("display_order");
    Integer answerPrecedence = rsw.getInt("answer_precedence");
    Boolean dividerAbove = rsw.getBoolean("divider_above");
    String nestedContent = rsw.getString("nested_content");
    String moreInfoContent = rsw.getString("more_info_content");
    return new StageAnswer()
        .setId(id)
        .setParentStageId(parentStageId)
        .setGoToStageId(goToStageId)
        .setGoToStageAnswerOutcomeType(goToStageAnswerOutcomeType)
        .setControlEntryId(controlEntryId)
        .setAnswerText(answerText)
        .setDisplayOrder(displayOrder)
        .setAnswerPrecedence(answerPrecedence)
        .setDividerAbove(dividerAbove)
        .setNestedContent(nestedContent)
        .setMoreInfoContent(moreInfoContent);
  }
}
