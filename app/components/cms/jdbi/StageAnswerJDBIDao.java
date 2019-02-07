package components.cms.jdbi;

import components.cms.mapper.StageAnswerRSMapper;
import models.cms.StageAnswer;
import models.cms.enums.OutcomeType;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import java.util.List;

public interface StageAnswerJDBIDao {

  @Mapper(StageAnswerRSMapper.class)
  @SqlQuery("SELECT * FROM stage_answer WHERE go_to_stage_id = :goToStageId")
  StageAnswer getStageAnswerByGoToStageId(@Bind("goToStageId") long goToStageId);

  @Mapper(StageAnswerRSMapper.class)
  @SqlQuery("SELECT * FROM stage_answer WHERE stage_id = :stageId")
  List<StageAnswer> getForStageId(@Bind("stageId") long stageId);

  @SqlQuery(
      "INSERT INTO stage_answer (stage_id, go_to_stage_id, go_to_outcome_type, control_entry_id, answer_text, display_order, answer_precedence, nested_content, more_info_content) "
          + "VALUES(:stageId, :goToStageId, :goToOutcomeType, :controlEntryId, :answerText, :displayOrder, :answerPrecedence, :nestedContent, :moreInfoContent) "
          + "RETURNING id")
  Long insert(
      @Bind("stageId") Long stageId,
      @Bind("goToStageId") Long goToStageId,
      @Bind("goToOutcomeType") OutcomeType outcomeType,
      @Bind("controlEntryId") Long controlEntryId,
      @Bind("answerText") String answerText,
      @Bind("displayOrder") Integer displayOrder,
      @Bind("answerPrecedence") Integer answerPrecedence,
      @Bind("nestedContent") String nestedContent,
      @Bind("moreInfoContent") String moreInfoContent);

  @SqlUpdate("DELETE FROM stage_answer")
  void truncate();

}
