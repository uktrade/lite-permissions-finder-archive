package journey;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import models.GoodsType;
import models.softtech.SoftTechCategory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SoftTechCategoryDecider implements Decider<SoftTechCategory> {

  private final PermissionsFinderDao dao;

  @Inject
  public SoftTechCategoryDecider(PermissionsFinderDao dao) {
    this.dao = dao;
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Override
  public CompletionStage<SoftTechCategory> decide() {

    GoodsType goodsType = dao.getGoodsType().get();

    SoftTechCategory softTechCategory = dao.getSoftTechCategory(goodsType).get();

    return CompletableFuture.completedFuture(softTechCategory);
  }

}
