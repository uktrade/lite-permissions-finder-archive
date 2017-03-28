package models.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class TransactionInfo {
  private final String transactionId;
  private final long ttl;
  private final String link;
  private final Map<String, String> fields;
  private final Date lastAccessed;

  public TransactionInfo(String transactionId, long ttl, String link, Date lastAccessed, Map<String, String> fields) {
    this.transactionId = transactionId;
    this.ttl = ttl;
    this.fields = fields;
    this.link = link;
    this.lastAccessed = lastAccessed;
  }

  public TransactionInfo(String transactionId, long ttl, String link, Date lastAccessed) {
    this(transactionId, ttl, link, lastAccessed, null);
  }

  public String getTransactionId() {
    return transactionId;
  }

  public long getTtl() {
    return ttl;
  }

  public String getLink() {
    return link;
  }

  public String getLastAccessed() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastAccessed);
  }

  @JsonInclude(Include.NON_NULL)
  public Map<String, String> getFields() {
    return fields;
  }
}
