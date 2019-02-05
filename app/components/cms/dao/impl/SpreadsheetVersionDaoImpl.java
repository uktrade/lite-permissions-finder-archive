package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.SpreadsheetVersionDao;
import components.cms.jdbi.SpreadsheetVersionJDBIDao;
import models.cms.SpreadsheetVersion;
import org.skife.jdbi.v2.DBI;

public class SpreadsheetVersionDaoImpl implements SpreadsheetVersionDao {

  private final SpreadsheetVersionJDBIDao spreadsheetVersionJDBIDao;

  @Inject
  public SpreadsheetVersionDaoImpl(DBI dbi) {
    this.spreadsheetVersionJDBIDao = dbi.onDemand(SpreadsheetVersionJDBIDao.class);
  }

  @Override
  public void insert(String filename, String version, String sha1) {
    spreadsheetVersionJDBIDao.insert(filename, version, sha1);
  }

  @Override
  public SpreadsheetVersion getLatestSpreadsheetVersion() {
    return spreadsheetVersionJDBIDao.getLatestSpreadsheetVersion();
  }

}
