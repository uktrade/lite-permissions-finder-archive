package models.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Map;

public class TransactionInfo {
  private final String transactionId;
  private final long ttl;
  private final String link;
  private final Map<String, String> fields;

  public TransactionInfo(String transactionId, long ttl, String link, Map<String, String> fields) {
    this.transactionId = transactionId;
    this.ttl = ttl;
    this.fields = fields;
    this.link = link;
  }

  public TransactionInfo(String transactionId, long ttl, String link) {
    this(transactionId, ttl, link, null);
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

  @JsonInclude(Include.NON_NULL)
  public Map<String, String> getFields() {
    return fields;
  }
}
