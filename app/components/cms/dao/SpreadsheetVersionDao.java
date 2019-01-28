package components.cms.dao;

import models.cms.SpreadsheetVersion;

public interface SpreadsheetVersionDao {

  void insert(String version);

  SpreadsheetVersion getLatestSpreadsheetVersion();

}
