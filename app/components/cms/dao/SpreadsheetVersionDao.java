package components.cms.dao;

import models.cms.SpreadsheetVersion;

public interface SpreadsheetVersionDao {

  void insert(String filename, String version, String sha1);

  SpreadsheetVersion getLatestSpreadsheetVersion();

}
