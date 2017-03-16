package components.persistence;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.persistence.RedisKeyConfig;
import controllers.admin.routes;
import models.persistence.TransactionInfo;
import models.persistence.ApplicationCodeInfo;
import play.mvc.Http;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminDao {
  private final JedisPool jedisPool;
  private final RedisKeyConfig appCodeKeyConfig;
  private final RedisKeyConfig pfKeyConfig;

  private final static int SCAN_COUNT = 100;

  @Inject
  public AdminDao(JedisPool jedisPool, @Named("applicationCodeDaoHash") RedisKeyConfig appCodeKeyConfig, @Named("permissionsFinderDaoHash") RedisKeyConfig pfKeyConfig) {
    this.jedisPool = jedisPool;
    this.appCodeKeyConfig = appCodeKeyConfig;
    this.pfKeyConfig = pfKeyConfig;
  }

  public List<String> scanHashKeys(Jedis jedis, String pattern) {
    List<String> keys = new ArrayList<>();
    ScanParams scanParams = new ScanParams().count(SCAN_COUNT).match(pattern);
    boolean moreKeys = true;
    String cursor = ScanParams.SCAN_POINTER_START;

    while (moreKeys) {
      ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
      keys.addAll(scanResult.getResult());
      cursor = scanResult.getStringCursor();
      if (cursor.equals(ScanParams.SCAN_POINTER_START)) {
        moreKeys = false;
      }
    }

    return keys;
  }

  public List<TransactionInfo> getPermissionsFinderTransactions() {
    try (Jedis jedis = jedisPool.getResource()) {
      String pattern = pfKeyConfig.getKeyPrefix() + ":*:" + pfKeyConfig.getHashName();
      boolean showFields = false;
      return scanHashKeys(jedis, pattern).stream()
          .map(key -> buildTransactionInfo(key, jedis, showFields))
          .sorted(Comparator.comparingLong(TransactionInfo::getTtl).reversed())
          .collect(Collectors.toList());
    }
  }

  public TransactionInfo getPermissionsFinderTransactionById(String id) {
    try (Jedis jedis = jedisPool.getResource()) {
      String key = pfKeyConfig.getKeyPrefix() + ":" + id + ":" + pfKeyConfig.getHashName();
      boolean showFields = true;
      if (jedis.exists(key)) {
        return buildTransactionInfo(key, jedis, showFields);
      }
      else {
        return null;
      }
    }
  }
  
  public List<ApplicationCodeInfo> getApplicationCodes() {
    try (Jedis jedis = jedisPool.getResource()) {
      String pattern = appCodeKeyConfig.getKeyPrefix() + ":" + appCodeKeyConfig.getHashName() + ":*";
      boolean showFields = false;
      return scanHashKeys(jedis, pattern).stream()
          .map(key -> buildApplicationCodeInfo(key, jedis, showFields))
          .sorted(Comparator.comparingLong(ApplicationCodeInfo::getTtl).reversed())
          .collect(Collectors.toList());
    }
  }

  public ApplicationCodeInfo getApplicationCodeByCode(String applicationCode) {
    try (Jedis jedis = jedisPool.getResource()) {
      String key = appCodeKeyConfig.getKeyPrefix() + ":" + appCodeKeyConfig.getHashName() + ":" + applicationCode;
      boolean showFields = true;
      if (jedis.exists(key)) {
        return buildApplicationCodeInfo(key, jedis, showFields);
      }
      else {
       return null;
      }
    }
  }

  private TransactionInfo buildTransactionInfo(String key, Jedis jedis, boolean showFields) {
    String transactionId = key.split(":")[1];
    Long ttl = jedis.ttl(key);
    String link = getPermissionsFinderTransactionByIdUrl(transactionId);
    if (showFields) {
      Map<String, String> fields = jedis.hgetAll(key);
      return new TransactionInfo(transactionId, ttl, link, fields);
    }
    else {
      return new TransactionInfo(transactionId, ttl, link);
    }
  }

  private ApplicationCodeInfo buildApplicationCodeInfo(String key, Jedis jedis, boolean showFields) {
    String applicationCode = key.split(":")[2];
    Long ttl = jedis.ttl(key);
    Map<String, String> fields = jedis.hgetAll(key);

    Optional<Map.Entry<String, String>> transactionIdOptional = fields.entrySet().stream()
        .filter(e -> "transactionId".equals(e.getKey()))
        .findAny();
    String transactionId = transactionIdOptional.isPresent() ? transactionIdOptional.get().getValue() : "";

    String linkToTransaction = getPermissionsFinderTransactionByIdUrl(transactionId);
    String linkToApplicationCode = getApplicationCodeByCodeUrl(applicationCode);

    if (showFields) {
      return new ApplicationCodeInfo(applicationCode, ttl, linkToTransaction, linkToApplicationCode, fields);
    }
    else {
      return new ApplicationCodeInfo(applicationCode, ttl, linkToTransaction, linkToApplicationCode);
    }
  }

  private String getPermissionsFinderTransactionByIdUrl(String transactionId) {
     Http.Context ctx = Http.Context.current();
     return routes.AdminController.permissionsFinderTransactionById(transactionId).absoluteURL(ctx._requestHeader());
  }

  private String getApplicationCodeByCodeUrl(String applicationCode) {
    Http.Context ctx = Http.Context.current();
    return routes.AdminController.applicationCodeByCode(applicationCode).absoluteURL(ctx._requestHeader());
  }
}
