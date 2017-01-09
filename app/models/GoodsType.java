package models;

import java.util.EnumSet;
import java.util.Optional;

public enum GoodsType {
  PHYSICAL,
  SOFTWARE,
  TECHNOLOGY;
  
  private String value;
  private String urlString;

  GoodsType() {
    this.value = this.toString();
    this.urlString = value.toLowerCase();
  }

  public String value() {
    return this.value;
  }

  public String urlString(){
    return urlString;
  }

  public static Optional<GoodsType> getMatchedByValue(String goodsTypeValue) {
    return EnumSet.allOf(GoodsType.class).stream().filter(e -> e.value().equals(goodsTypeValue)).findFirst();
  }

  public static Optional<GoodsType> getMatchedByUrlString(String goodsTypeUrlString) {
    return EnumSet.allOf(GoodsType.class).stream().filter(e -> e.urlString().equals(goodsTypeUrlString)).findFirst();
  }
}
