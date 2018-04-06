package components.persistence;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.persistence.CommonRedisDao;
import components.common.persistence.RedisKeyConfig;
import components.common.transaction.TransactionManager;
import org.redisson.api.RedissonClient;

public class ImportJourneyDao {

  private final CommonRedisDao commonRedisDao;

  private static final String IMPORT_COUNTRY_SELECTED_KEY = "IMPORT_COUNTRY_SELECTED_KEY";

  @Inject
  public ImportJourneyDao(@Named("permissionsFinderDaoHash") RedisKeyConfig keyConfig, RedissonClient redissonClient,
                          TransactionManager transactionManager) {
    this.commonRedisDao = new CommonRedisDao(keyConfig, redissonClient, transactionManager);
  }

  public void saveImportCountrySelected(String importCountrySelected) {
    commonRedisDao.writeString(IMPORT_COUNTRY_SELECTED_KEY, importCountrySelected);
  }

  public String getImportCountrySelected() {
    return commonRedisDao.readString(IMPORT_COUNTRY_SELECTED_KEY);
  }

}
