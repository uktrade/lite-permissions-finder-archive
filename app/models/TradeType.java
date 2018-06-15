package models;

import org.apache.commons.lang3.EnumUtils;

public enum TradeType {

  IMPORT("From another country to the UK"),
  EXPORT("From the UK to another country"),
  BROKERING("Between non-UK countries"),
  TRANSSHIPMENT("Through the UK, from one non-UK country to another");

  private final String title;

  TradeType(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public static String getTitle(String type) {
    TradeType tt = EnumUtils.getEnum(TradeType.class, type.toUpperCase());
    return tt != null ? tt.getTitle() : "";
  }
}
