package journey.deciders;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.relationships.GoodsRelationshipsServiceClient;
import components.services.controlcode.relationships.GoodsRelationshipsServiceResult;
import models.GoodsType;
import models.softtech.SoftTechCategory;
import play.libs.concurrent.HttpExecutionContext;

import java.util.concurrent.CompletionStage;

public class RelationshipWithSoftwareDecider implements Decider<Boolean> {

  private final GoodsRelationshipsServiceClient goodsRelationshipsServiceClient;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public RelationshipWithSoftwareDecider(GoodsRelationshipsServiceClient goodsRelationshipsServiceClient, PermissionsFinderDao permissionsFinderDao, HttpExecutionContext httpExecutionContext) {
    this.goodsRelationshipsServiceClient = goodsRelationshipsServiceClient;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
  }

  @Override
  public CompletionStage<Boolean> decide() {
    GoodsType goodsType = permissionsFinderDao.getGoodsType().get();
    SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
    return goodsRelationshipsServiceClient.get(goodsType, GoodsType.SOFTWARE, softTechCategory)
        .thenApplyAsync(GoodsRelationshipsServiceResult::relationshipsExist, httpExecutionContext.current());
  }
}
