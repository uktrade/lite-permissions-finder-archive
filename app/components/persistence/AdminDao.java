package components.persistence;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.persistence.RedisKeyConfig;
import controllers.admin.routes;
import models.admin.ApplicationCodeInfo;
import models.admin.TransactionInfo;
import org.redisson.api.RedissonClient;
import play.mvc.Http;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminDao {
  private final RedissonClient redissonClient;
  private final RedisKeyConfig appCodeKeyConfig;
  private final RedisKeyConfig pfKeyConfig;

  @Inject
  public AdminDao(RedissonClient redissonClient, @Named("applicationCodeDaoHash") RedisKeyConfig appCodeKeyConfig,
                  @Named("permissionsFinderDaoHash") RedisKeyConfig pfKeyConfig) {
    this.redissonClient = redissonClient;
    this.appCodeKeyConfig = appCodeKeyConfig;
    this.pfKeyConfig = pfKeyConfig;
  }

  public List<TransactionInfo> getPermissionsFinderTransactions() {
    String pattern = pfKeyConfig.getKeyPrefix() + ":*:" + pfKeyConfig.getHashName();
    return redissonClient.getKeys()
        .findKeysByPattern(pattern)
        .stream()
        .map(key -> buildTransactionInfo(key, false))
        .sorted(Comparator.comparingLong(TransactionInfo::getTtl).reversed())
        .collect(Collectors.toList());
  }

  public TransactionInfo getPermissionsFinderTransactionById(String id) {
    String key = pfKeyConfig.getKeyPrefix() + ":" + id + ":" + pfKeyConfig.getHashName();
    if (redissonClient.getMap(key).isExists()) {
      return buildTransactionInfo(key, true);
    } else {
      return null;
    }
  }

  public List<ApplicationCodeInfo> getApplicationCodes() {
    String pattern = appCodeKeyConfig.getKeyPrefix() + ":" + appCodeKeyConfig.getHashName() + ":*";
    return redissonClient.getKeys()
        .findKeysByPattern(pattern)
        .stream()
        .map(key -> buildApplicationCodeInfo(key, false))
        .sorted(Comparator.comparingLong(ApplicationCodeInfo::getTtl).reversed())
        .collect(Collectors.toList());
  }

  public ApplicationCodeInfo getApplicationCodeByCode(String applicationCode) {
    String key = appCodeKeyConfig.getKeyPrefix() + ":" + appCodeKeyConfig.getHashName() + ":" + applicationCode;
    if (redissonClient.getMap(key).isExists()) {
      return buildApplicationCodeInfo(key, true);
    } else {
      return null;
    }
  }

  private TransactionInfo buildTransactionInfo(String key, boolean showFields) {
    String transactionId = key.split(":")[1];
    Long ttl = redissonClient.getMap(key).remainTimeToLive();
    String link = getPermissionsFinderTransactionByIdUrl(transactionId);
    if (showFields) {
      Map<String, String> fields = redissonClient.getMap(key);
      return new TransactionInfo(transactionId, ttl, link, fields);
    } else {
      return new TransactionInfo(transactionId, ttl, link);
    }
  }

  private ApplicationCodeInfo buildApplicationCodeInfo(String key, boolean showFields) {
    String applicationCode = key.split(":")[2];
    Long ttl = redissonClient.getMap(key).remainTimeToLive();
    Map<String, String> fields = redissonClient.getMap(key);

    Optional<Map.Entry<String, String>> transactionIdOptional = fields.entrySet().stream()
        .filter(e -> "transactionId".equals(e.getKey()))
        .findAny();
    String transactionId = transactionIdOptional.isPresent() ? transactionIdOptional.get().getValue() : "";

    String linkToTransaction = getPermissionsFinderTransactionByIdUrl(transactionId);
    String linkToApplicationCode = getApplicationCodeByCodeUrl(applicationCode);

    if (showFields) {
      return new ApplicationCodeInfo(applicationCode, ttl, linkToTransaction, linkToApplicationCode, fields);
    } else {
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
