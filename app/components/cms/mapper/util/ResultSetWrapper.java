package components.cms.mapper.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetWrapper {
  private final ResultSet resultSet;

  public ResultSetWrapper(ResultSet resultSet){
    this.resultSet = resultSet;
  }

  public Integer getInt(String columnLabel) throws SQLException {
    int resultInt = resultSet.getInt(columnLabel);
    if (resultSet.wasNull()) {
      return null;
    } else {
      return resultInt;
    }
  }

  public Long getLong(String columnLabel) throws SQLException {
    long resultLong = resultSet.getLong(columnLabel);
    if (resultSet.wasNull()) {
      return null;
    } else {
      return resultLong;
    }
  }

  public String getString(String columnLabel) throws SQLException {
    return resultSet.getString(columnLabel);
  }

  public Boolean getBoolean(String columnLabel) throws SQLException {
    boolean resultBool = resultSet.getBoolean(columnLabel);
    if (resultSet.wasNull()) {
      return null;
    } else {
      return resultBool;
    }
  }
}
