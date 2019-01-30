package components.cms.mapper;

import models.cms.SpreadsheetVersion;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SpreadsheetVersionRSMapper implements ResultSetMapper<SpreadsheetVersion> {

  @Override
  public SpreadsheetVersion map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    long id = r.getLong("id");
    String filename = r.getString("filename");
    String version = r.getString("version");
    String sha1 = r.getString("sha1");
    return new SpreadsheetVersion(id, filename, version, sha1);
  }

}
