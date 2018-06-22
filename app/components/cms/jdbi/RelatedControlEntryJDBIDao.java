package components.cms.jdbi;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface RelatedControlEntryJDBIDao {
  @SqlUpdate("INSERT INTO related_control_entry (control_entry_id,  related_control_entry_id) " +
      "VALUES (:controlEntryId, :relatedControlEntryId)")
  void insert(@Bind("controlEntryId") long controlEntryId,
              @Bind("relatedControlEntryId") long relatedControlEntryId);

  @SqlUpdate("DELETE FROM related_control_entry")
  void truncate();
}
