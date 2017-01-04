package journey;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import models.GoodsType;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class GoodsTypeDecider  implements Decider<GoodsType> {

  private final PermissionsFinderDao dao;

  @Inject
  public GoodsTypeDecider(PermissionsFinderDao dao) {
    this.dao = dao;
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Override
  public CompletionStage<GoodsType> decide() {

    Optional<GoodsType> goodsTypeOptional = dao.getGoodsType();

    return CompletableFuture.completedFuture(goodsTypeOptional.get());
  }

}
