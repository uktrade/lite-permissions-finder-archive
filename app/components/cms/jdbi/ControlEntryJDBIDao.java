package components.cms.jdbi;

import components.cms.mapper.ControlEntryRSMapper;
import models.cms.ControlEntry;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import java.util.List;

public interface ControlEntryJDBIDao {

  @Mapper(ControlEntryRSMapper.class)
  @SqlQuery("SELECT * FROM control_entry WHERE id = :id")
  ControlEntry get(@Bind("id") long id);

  @Mapper(ControlEntryRSMapper.class)
  @SqlQuery("SELECT * FROM control_entry WHERE parent_control_entry_id = :parentId")
  List<ControlEntry> getChildren(@Bind("parentId") long parentId);

  @SqlQuery(
      "INSERT INTO control_entry (parent_control_entry_id, control_code, full_description, summary_description, nested, selectable, regime) "
          + "VALUES (:parentControlEntryId, :controlCode, :fullDescription, :summaryDescription, :nested, :selectable, :regime) "
          + "RETURNING id")
  Long insert(
      @Bind("parentControlEntryId") Long parentControlEntryId,
      @Bind("controlCode") String controlCode,
      @Bind("fullDescription") String fullDescription,
      @Bind("summaryDescription") String summaryDescription,
      @Bind("nested") Boolean nested,
      @Bind("selectable") Boolean selectable,
      @Bind("regime") String regime
  );

  @SqlUpdate("DELETE FROM control_entry WHERE id = :id")
  void delete(@Bind("id") long id);

  @SqlUpdate("DELETE FROM control_entry")
  void truncate();

}
