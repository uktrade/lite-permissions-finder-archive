package models.admin;

public class PingAuditResult {

  private String status;
  private String detail;

  private final String OK = "OK";
  private final String NOT_OK = "NOT_OK";

  public PingAuditResult() {
    this.status = NOT_OK;
    this.detail = "";
  }

  public void setStatusOk() {
    this.status = OK;
  }

  public void addDetailPart(String title, boolean isOk) {
    String okOrNot = isOk? OK : NOT_OK;
    detail = detail + "[" + title + "(" + okOrNot + ")]";
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }
}
