package controllers.softtech;

import models.GoodsType;

public class Utils {

  public static void validateGoodsType(GoodsType goodsType) {
    if (goodsType != GoodsType.SOFTWARE && goodsType != goodsType.TECHNOLOGY) {
      throw new RuntimeException(String.format("Expecting SOFTWARE or TECHNOLOGY, instead got \"%s\"", goodsType.toString()));
    }
  }
}
