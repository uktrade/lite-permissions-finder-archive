package components.persistence;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.transaction.TransactionManager;
import play.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.concurrent.TimeUnit;

public class ApplicationCodeDao {

  public static final String KEY_PREFIX = "transactionLookup";
  public static final String FIELD_NAME = "transactionId";

  public final int ttlSeconds;
  public final JedisPool jedisPool;
  public final TransactionManager transactionManager;

  @Inject
  public ApplicationCodeDao(@Named("redisLookupTtlSeconds") int ttlSeconds, JedisPool jedisPool, TransactionManager transactionManager) {
    this.ttlSeconds = ttlSeconds;
    this.jedisPool = jedisPool;
    this.transactionManager = transactionManager;
  }

  /**
   * Writes the (Key, Value) pair (Application Code, Transaction Id)
   * @param applicationCode the key
   */
  public void writeTransactionId(String applicationCode) {
    String existingTransactionId = readTransactionId(applicationCode);
    String currentTransactionId = transactionManager.getTransactionId();
    Stopwatch stopwatch = Stopwatch.createStarted();
    try (Jedis jedis = jedisPool.getResource()) {
      if (existingTransactionId != null && !currentTransactionId.equals(existingTransactionId)) {
        throw new RuntimeException(String.format("Application code already in use for transaction '%s'", existingTransactionId));
      }
      Transaction multi = jedis.multi();
      multi.hset(hashKey(applicationCode), FIELD_NAME, transactionManager.getTransactionId());
      multi.expire(hashKey(applicationCode), ttlSeconds);
      multi.exec();
    }
    finally {
      Logger.debug(String.format("Write of '%s' string completed in %d ms", FIELD_NAME, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    }
  }

  /**
   * Read the Transaction Id (value) from the (Key, Value) pair (Application Code, Transaction Id)
   * @param applicationCode the key
   * @return the corresponding Transaction Id (value)
   */
  public final String readTransactionId(String applicationCode) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    String transactionId;
    try (Jedis jedis = jedisPool.getResource()) {
      transactionId = jedis.hget(hashKey(applicationCode), FIELD_NAME);
    }
    finally {
      Logger.debug(String.format("Read of '%s' string completed in %d ms", FIELD_NAME, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    }
    return transactionId;
  }

  public String hashKey(String applicationCode) {
    return KEY_PREFIX + ":" + applicationCode;
  }

}
