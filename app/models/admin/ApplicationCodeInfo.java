package models.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ApplicationCodeInfo {
  private final String applicationCode;
  private final long ttl;
  private final Map<String, String> links;
  private final Date lastAccessed;
  private final Map<String, String> fields;

  public ApplicationCodeInfo(String applicationCode, long ttl, String linkToTransaction, String linkToApplicationCode, Date lastAccessed, Map<String, String> fields) {
    this.applicationCode = applicationCode;
    this.ttl = ttl;
    this.lastAccessed = lastAccessed;
    this.fields = fields;
    links = new HashMap<>();
    links.put("transaction", linkToTransaction);
    links.put("applicationCode", linkToApplicationCode);
  }

  public ApplicationCodeInfo(String applicationCode, long ttl, String linkToTransaction, String linkToApplicationCode, Date lastAccessed) {
    this(applicationCode, ttl, linkToTransaction, linkToApplicationCode, lastAccessed,null);
  }

  public String getApplicationCode() {
    return applicationCode;
  }

  public long getTtl() {
    return ttl;
  }

  public Map<String, String> getLinks() {
    return links;
  }

  public String getLastAccessed() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastAccessed);
  }

  @JsonInclude(Include.NON_NULL)
  public Map<String, String> getFields() {
    return fields;
  }
}
