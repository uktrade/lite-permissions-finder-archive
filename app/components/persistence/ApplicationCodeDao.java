package components.persistence;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.persistence.RedisKeyConfig;
import components.common.transaction.TransactionManager;
import org.redisson.api.RedissonClient;
import play.Logger;

import java.util.concurrent.TimeUnit;

public class ApplicationCodeDao {

  private static final String FIELD_NAME = "transactionId";

  private final RedissonClient redissonClient;
  private final RedisKeyConfig redisKeyConfig;
  private final TransactionManager transactionManager;

  @Inject
  public ApplicationCodeDao(@Named("applicationCodeDaoHash") RedisKeyConfig redisKeyConfig,
                            RedissonClient redissonClient, TransactionManager transactionManager) {
    this.redissonClient = redissonClient;
    this.redisKeyConfig = redisKeyConfig;
    this.transactionManager = transactionManager;
  }

  /**
   * Writes the (Key, Value) pair (Application Code, Transaction Id)
   *
   * @param applicationCode the key
   */
  public void writeTransactionId(String applicationCode) {
    String existingTransactionId = readTransactionId(applicationCode);
    String currentTransactionId = transactionManager.getTransactionId();
    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      if (existingTransactionId != null && !currentTransactionId.equals(existingTransactionId)) {
        throw new RuntimeException(String.format("Application code already in use for transaction '%s'", existingTransactionId));
      }
      redissonClient.getMap(hashKey(applicationCode)).expire(redisKeyConfig.getHashTtlSeconds(), TimeUnit.SECONDS);
      redissonClient.getMap(hashKey(applicationCode)).put(FIELD_NAME, transactionManager.getTransactionId());
    } finally {
      Logger.debug(String.format("Write of '%s' string completed in %d ms", FIELD_NAME, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    }
  }

  /**
   * Read the Transaction Id (value) from the (Key, Value) pair (Application Code, Transaction Id)
   *
   * @param applicationCode the key
   * @return the corresponding Transaction Id (value)
   */
  public final String readTransactionId(String applicationCode) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      return (String) redissonClient.getMap(hashKey(applicationCode)).get(FIELD_NAME);
    } finally {
      Logger.debug(String.format("Read of '%s' string completed in %d ms", FIELD_NAME, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    }
  }

  /**
   * Refreshes the TTL of the key associated with <code>applicationCode</code>
   *
   * @param applicationCode the key
   */
  public void refreshTTL(String applicationCode) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      boolean reply = redissonClient.getMap(hashKey(applicationCode)).expire(redisKeyConfig.getHashTtlSeconds(), TimeUnit.SECONDS);
      if (!reply) {
        Logger.error(String.format("Could not refresh TTL of '%s', key does not exist", FIELD_NAME));
      }
    } finally {
      Logger.debug(String.format("TTL of '%s' refresh completed in %d ms", FIELD_NAME, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    }
  }

  private String hashKey(String applicationCode) {
    return redisKeyConfig.getKeyPrefix() + ":" + redisKeyConfig.getHashName() + ":" + applicationCode;
  }

}
