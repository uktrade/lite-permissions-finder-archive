package models;

import java.util.EnumSet;
import java.util.Optional;

public enum GoodsType {
  PHYSICAL("PHYSICAL"),
  SOFTWARE("SOFTWARE"),
  TECHNOLOGY("TECHNOLOGY");
  
  private String value;

  GoodsType(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public String toUrlString(){
    return this.toString().toLowerCase();
  }

  public static Optional<GoodsType> getMatched(String goodsType) {
    return EnumSet.allOf(GoodsType.class).stream().filter(e -> e.value().equals(goodsType)).findFirst();
  }
}
