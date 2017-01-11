package journey.deciders;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.controls.relationships.GoodsRelationshipsServiceClient;
import components.services.controlcode.controls.relationships.GoodsRelationshipsServiceResult;
import models.GoodsType;

import java.util.concurrent.CompletionStage;

public class RelationshipWithSoftwareDecider implements Decider<Boolean> {

  private final GoodsRelationshipsServiceClient goodsRelationshipsServiceClient;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public RelationshipWithSoftwareDecider(GoodsRelationshipsServiceClient goodsRelationshipsServiceClient, PermissionsFinderDao permissionsFinderDao) {
    this.goodsRelationshipsServiceClient = goodsRelationshipsServiceClient;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  @Override
  public CompletionStage<Boolean> decide() {
    GoodsType goodsType = permissionsFinderDao.getGoodsType().get();
    return goodsRelationshipsServiceClient.get(goodsType, GoodsType.TECHNOLOGY) // TODO switch to GoodsType.SOFTWARE after service is fixed
        .thenApply(GoodsRelationshipsServiceResult::relationshipsExist);
  }
}
