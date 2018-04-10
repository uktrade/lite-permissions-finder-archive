package components.persistence;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.persistence.CommonRedisDao;

public class ImportJourneyDao {

  private final CommonRedisDao commonRedisDao;

  private static final String IMPORT_COUNTRY_SELECTED_KEY = "IMPORT_COUNTRY_SELECTED_KEY";

  @Inject
  public ImportJourneyDao(@Named("permissionsFinderDaoHashCommon") CommonRedisDao commonRedisDao) {
    this.commonRedisDao = commonRedisDao;
  }

  public void saveImportCountrySelected(String importCountrySelected) {
    commonRedisDao.writeString(IMPORT_COUNTRY_SELECTED_KEY, importCountrySelected);
  }

  public String getImportCountrySelected() {
    return commonRedisDao.readString(IMPORT_COUNTRY_SELECTED_KEY);
  }

}
