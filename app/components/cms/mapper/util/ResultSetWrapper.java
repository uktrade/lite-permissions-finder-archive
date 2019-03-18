package components.cms.mapper.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class ResultSetWrapper {

  private final ResultSet resultSet;

  public ResultSetWrapper(ResultSet resultSet) {
    this.resultSet = resultSet;
  }

  public Integer getInt(String columnLabel) throws SQLException {
    int resultInt = resultSet.getInt(columnLabel);
    return !resultSet.wasNull() ? resultInt : null;
  }

  public Long getLong(String columnLabel) throws SQLException {
    long resultLong = resultSet.getLong(columnLabel);
    return !resultSet.wasNull() ? resultLong : null;
  }

  public List<String> getStrings(String columnLabel) throws SQLException {
    String commaSeparatedStringList = resultSet.getString(columnLabel);
    return !resultSet.wasNull() ? Arrays
      .stream(commaSeparatedStringList.split(","))
      .map(String::trim)
      .filter(StringUtils::isNotEmpty)
      .collect(Collectors.toList())
      : Collections.EMPTY_LIST;
  }
}
