package models.cms;

import lombok.Data;

@Data
public class SpreadsheetVersion {
  private final Long id;
  private final String version;

  public SpreadsheetVersion(String version) {
    this.id = null;
    this.version = version;
  }

  public SpreadsheetVersion(Long id, String version) {
    this.id = id;
    this.version = version;
  }
}
