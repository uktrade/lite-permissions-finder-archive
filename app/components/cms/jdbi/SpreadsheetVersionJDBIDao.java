package components.cms.jdbi;

import components.cms.mapper.SpreadsheetVersionRSMapper;
import models.cms.SpreadsheetVersion;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

public interface SpreadsheetVersionJDBIDao {

  @SqlUpdate("INSERT INTO SPREADSHEET_VERSION (version) VALUES (:version)")
  void insert(@Bind("version") String version);

  @RegisterMapper(SpreadsheetVersionRSMapper.class)
  @SqlQuery("SELECT * FROM SPREADSHEET_VERSION ORDER BY TIMESTAMP DESC LIMIT 1")
  SpreadsheetVersion getLatestSpreadsheetVersion();

}
