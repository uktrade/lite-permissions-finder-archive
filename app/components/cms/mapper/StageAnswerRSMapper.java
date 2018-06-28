package components.cms.mapper;

import components.cms.mapper.util.ResultSetWrapper;
import models.cms.StageAnswer;
import models.cms.enums.OutcomeType;
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
    Long stageId = rsw.getLong("stage_id");
    Long goToStageId = rsw.getLong("go_to_stage_id");
    String outcomeTypeStr = r.getString("go_to_outcome_type");
    OutcomeType outcomeType = EnumUtils.getEnum(OutcomeType.class, outcomeTypeStr);
    Long controlEntryId = rsw.getLong("control_entry_id");
    String answerText = r.getString("answer_text");
    Integer displayOrder = rsw.getInt("display_order");
    Integer answerPrecedence = rsw.getInt("answer_precedence");
    boolean dividerAbove = r.getBoolean("divider_above");
    String nestedContent = r.getString("nested_content");
    String moreInfoContent = r.getString("more_info_content");
    return new StageAnswer()
        .setId(id)
        .setStageId(stageId)
        .setGoToStageId(goToStageId)
        .setGoToOutcomeType(outcomeType)
        .setControlEntryId(controlEntryId)
        .setAnswerText(answerText)
        .setDisplayOrder(displayOrder)
        .setAnswerPrecedence(answerPrecedence)
        .setDividerAbove(dividerAbove)
        .setNestedContent(nestedContent)
        .setMoreInfoContent(moreInfoContent);
  }
}
