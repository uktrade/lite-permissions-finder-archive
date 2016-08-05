package components.persistence;

import com.google.inject.Inject;
import components.common.persistence.CommonRedisDao;
import components.common.persistence.RedisKeyConfig;
import components.common.transaction.TransactionManager;
import redis.clients.jedis.JedisPool;

public class PermissionsFinderDao extends CommonRedisDao {

  @Inject
  public PermissionsFinderDao(RedisKeyConfig keyConfig, JedisPool pool, TransactionManager transactionManager) {
    super(keyConfig, pool, transactionManager);
  }

  public void savePhysicalGoodControlCode(String physicalGoodControlCode) {
    writeString("physicalGoodControlCode", physicalGoodControlCode);
  }

  public String getPhysicalGoodControlCode() {
    return readString("physicalGoodControlCode");
  }

}
