package components.persistence;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.persistence.CommonRedisDao;
import components.common.persistence.RedisKeyConfig;
import components.common.transaction.TransactionManager;
import redis.clients.jedis.JedisPool;

public class ImportJourneyDao extends CommonRedisDao {

  private static final String IMPORT_COUNTRY_SELECTED_KEY = "IMPORT_COUNTRY_SELECTED_KEY";

  @Inject
  public ImportJourneyDao(@Named("permissionsFinderDaoHash") RedisKeyConfig keyConfig, JedisPool pool, TransactionManager transactionManager) {
    super(keyConfig, pool, transactionManager);
  }

  public void saveImportCountrySelected(String importCountrySelected) {
    writeString(IMPORT_COUNTRY_SELECTED_KEY, importCountrySelected);
  }

  public String getImportCountrySelected() {
    return readString(IMPORT_COUNTRY_SELECTED_KEY);
  }

}
