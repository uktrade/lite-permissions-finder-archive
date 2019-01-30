package models.cms;

import lombok.Data;

@Data
public class SpreadsheetVersion {
  private final Long id;
  private final String filename;
  private final String version;
  private final String sha1;

  public SpreadsheetVersion(String filename, String version, String sha1) {
    this.id = null;
    this.filename = filename;
    this.version = version;
    this.sha1 = sha1;
  }

  public SpreadsheetVersion(Long id, String filename, String version, String sha1) {
    this.id = id;
    this.filename = filename;
    this.version = version;
    this.sha1 = sha1;
  }
}
