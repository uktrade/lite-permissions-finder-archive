package journey.helpers;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.controls.ControlCode;
import components.services.controlcode.controls.catchall.CatchallControlsServiceClient;
import components.services.controlcode.controls.category.CategoryControlsServiceClient;
import components.services.controlcode.controls.related.RelatedControlsServiceClient;
import components.services.controlcode.controls.relationship.SoftwareAndTechnologyRelationshipServiceClient;
import journey.Events;
import models.GoodsType;
import models.controlcode.ControlCodeJourney;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.CatchallSoftTechControlsFlow;
import models.softtech.Relationship;
import models.softtech.SoftTechCatchallControlsNotApplicableFlow;
import models.softtech.SoftTechCategory;
import models.softtech.controls.SoftTechControlsJourney;
import org.apache.commons.lang3.StringUtils;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class SoftTechJourneyHelper {

  private final CategoryControlsServiceClient categoryControlsServiceClient;
  private final RelatedControlsServiceClient relatedControlsServiceClient;
  private final CatchallControlsServiceClient catchallControlsServiceClient;
  private final SoftwareAndTechnologyRelationshipServiceClient softwareAndTechnologyRelationshipServiceClient;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final JourneyManager journeyManager;

  @Inject
  public SoftTechJourneyHelper(CategoryControlsServiceClient categoryControlsServiceClient,
                               RelatedControlsServiceClient relatedControlsServiceClient,
                               CatchallControlsServiceClient catchallControlsServiceClient,
                               SoftwareAndTechnologyRelationshipServiceClient softwareAndTechnologyRelationshipServiceClient,
                               PermissionsFinderDao permissionsFinderDao,
                               HttpExecutionContext httpExecutionContext,
                               JourneyManager journeyManager) {
    this.categoryControlsServiceClient = categoryControlsServiceClient;
    this.relatedControlsServiceClient = relatedControlsServiceClient;
    this.catchallControlsServiceClient = catchallControlsServiceClient;
    this.softwareAndTechnologyRelationshipServiceClient = softwareAndTechnologyRelationshipServiceClient;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.journeyManager = journeyManager;
  }

  /**
   * Check for software controls of the given software category
   * Note: Writes to DAO if ONE would be returns and saveToDao is true. This is a small shortcut, preventing a
   * separate call to CategoryControlsServiceClient request the single control code to save.
   * @param goodsType The goods type to check the controls of, should be {@link GoodsType#SOFTWARE} or {@link GoodsType#TECHNOLOGY}
   * @param softTechCategory The software category to check the controls of
   * @param saveToDao Whether to save the single control code to the DAO, if a single control code were to be returned
   *                  by the CategoryControlsServiceClient request
   * @return The applicable software controls
   */
  public CompletionStage<ApplicableSoftTechControls> checkSoftTechControls(GoodsType goodsType, SoftTechCategory softTechCategory, boolean saveToDao) {
    return categoryControlsServiceClient.get(goodsType, softTechCategory)
        .thenApplyAsync(result -> {
          if (goodsType != GoodsType.SOFTWARE && goodsType != GoodsType.TECHNOLOGY) {
            throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\""
                , goodsType.toString()));
          }
          int size = result.controlCodes.size();
          if (size == 0) {
            return ApplicableSoftTechControls.ZERO;
          }
          else if (size == 1) {
            // Saving to the DAO here prevents a separate call to the CategoryControlsServiceClient, if not a little hacky
            if (saveToDao) {
              ControlCode controlCode = result.controlCodes.get(0);
              if (goodsType == GoodsType.SOFTWARE) {
                permissionsFinderDao.clearAndUpdateControlCodeJourneyDaoFieldsIfChanged(
                    ControlCodeJourney.SOFTWARE_CONTROLS, controlCode.controlCode);
              }
              else { // GoodsType.TECHNOLOGY
                permissionsFinderDao.clearAndUpdateControlCodeJourneyDaoFieldsIfChanged(
                    ControlCodeJourney.TECHNOLOGY_CONTROLS, controlCode.controlCode);
              }
            }
            return ApplicableSoftTechControls.ONE;
          }
          else if (size > 1) {
            return ApplicableSoftTechControls.GREATER_THAN_ONE;
          }
          else {
            throw new RuntimeException(String.format("Invalid value for size: \"%d\"", size));
          }
        }, httpExecutionContext.current());
  }

  public CompletionStage<ApplicableSoftTechControls> checkRelatedSoftwareControls(String controlCode, boolean saveToDao) {
    return relatedControlsServiceClient.get(GoodsType.SOFTWARE, controlCode) // TODO TECHNOLOGY
        .thenApplyAsync(result -> {
          int size = result.controlCodes.size();
          if (size == 0) {
            return ApplicableSoftTechControls.ZERO;
          }
          else if (size == 1) {
            // Saving to the DAO here prevents a separate call to the CategoryControlsServiceClient, if not a little hacky
            if (saveToDao) {
              ControlCode mappedControlCode = result.controlCodes.get(0);
              permissionsFinderDao.clearAndUpdateControlCodeJourneyDaoFieldsIfChanged(
                  ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD, mappedControlCode.controlCode);
            }
            return ApplicableSoftTechControls.ONE;
          }
          else if (size > 1) {
            return ApplicableSoftTechControls.GREATER_THAN_ONE;
          }
          else {
            throw new RuntimeException(String.format("Invalid value for size: \"%d\"", size));
          }
        }, httpExecutionContext.current());
  }

  public CompletionStage<ApplicableSoftTechControls> checkCatchtallSoftwareControls(GoodsType goodsType, SoftTechCategory softTechCategory, boolean saveToDao) {
    return catchallControlsServiceClient.get(goodsType, softTechCategory)
        .thenApplyAsync(result -> {
          int size = result.controlCodes.size();
          if (size == 0) {
            return ApplicableSoftTechControls.ZERO;
          }
          else if (size == 1) {
            // Saving to the DAO here prevents a separate call to the CatchallControlsServiceClient, if not a little hacky
            if (saveToDao) {
              ControlCode catchallControlCode = result.controlCodes.get(0);
              if (goodsType == GoodsType.SOFTWARE) {
                permissionsFinderDao.clearAndUpdateControlCodeJourneyDaoFieldsIfChanged(
                    ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS, catchallControlCode.controlCode);
              }
              else { // GoodsType.TECHNOLOGY
                permissionsFinderDao.clearAndUpdateControlCodeJourneyDaoFieldsIfChanged(
                    ControlCodeJourney.TECHNOLOGY_CATCHALL_CONTROLS, catchallControlCode.controlCode);
              }
            }
            return ApplicableSoftTechControls.ONE;
          }
          else if (size > 1) {
            return ApplicableSoftTechControls.GREATER_THAN_ONE;
          }
          else {
            throw new RuntimeException(String.format("Invalid value for size: \"%d\"", size));
          }
        }, httpExecutionContext.current());
  }

  public CompletionStage<Relationship> checkRelationshipExists(SoftTechCategory softTechCategory) {
    boolean relationshipExists = softTechCategory == SoftTechCategory.MILITARY;
    return softwareAndTechnologyRelationshipServiceClient.get(softTechCategory, relationshipExists)
        .thenApplyAsync(result -> {
          if (result.relationshipExists) {
            return Relationship.RELATIONSHIP_EXISTS;
          }
          else {
            return Relationship.RELATIONSHIP_DOES_NOT_EXIST;
          }
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> performCatchallSoftTechControlsTransition(GoodsType goodsType) {
    SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
    return checkCatchtallSoftwareControls(goodsType, softTechCategory, true)
        .thenComposeAsync(controls -> {
          if (controls == ApplicableSoftTechControls.ZERO) {
            return checkRelationshipExists(softTechCategory)
                .thenComposeAsync(relationship -> {
                  if (relationship == Relationship.RELATIONSHIP_EXISTS) {
                    return journeyManager.performTransition(Events.CATCHALL_SOFT_TECH_CONTROLS_FLOW,
                        CatchallSoftTechControlsFlow.RELATIONSHIP_EXISTS);
                  }
                  else if (relationship == Relationship.RELATIONSHIP_DOES_NOT_EXIST) {
                    return journeyManager.performTransition(Events.CATCHALL_SOFT_TECH_CONTROLS_FLOW,
                        CatchallSoftTechControlsFlow.RELATIONSHIP_DOES_NOT_EXIST);
                  }
                  else {
                    throw new RuntimeException(String.format("Unexpected member of Relationship enum: \"%s\""
                        , relationship.toString()));
                  }
                }, httpExecutionContext.current());
          }
          else if (controls == ApplicableSoftTechControls.ONE) {
            return journeyManager.performTransition(Events.CATCHALL_SOFT_TECH_CONTROLS_FLOW,
                CatchallSoftTechControlsFlow.CATCHALL_ONE);
          }
          else if (controls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
            return journeyManager.performTransition(Events.CATCHALL_SOFT_TECH_CONTROLS_FLOW,
                CatchallSoftTechControlsFlow.CATCHALL_GREATER_THAN_ONE);
          }
          else {
            throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
                , controls.toString()));
          }
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> performCatchallSoftTechControlNotApplicableTransition(GoodsType goodsType) {
    SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
    return checkCatchtallSoftwareControls(goodsType, softTechCategory, true)
        .thenComposeAsync(controls -> {
         if (controls == ApplicableSoftTechControls.ONE) {
           return checkRelationshipExists(softTechCategory)
               .thenComposeAsync(relationship -> {
                 if (relationship == Relationship.RELATIONSHIP_EXISTS) {
                   return journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CATCHALL_CONTROLS_NOT_APPLICABLE_FLOW,
                       SoftTechCatchallControlsNotApplicableFlow.RELATIONSHIP_EXISTS);
                 }
                 else if (relationship == Relationship.RELATIONSHIP_DOES_NOT_EXIST) {
                   return journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CATCHALL_CONTROLS_NOT_APPLICABLE_FLOW,
                       SoftTechCatchallControlsNotApplicableFlow.RELATIONSHIP_NOT_EXISTS);
                 }
                 else {
                   throw new RuntimeException(String.format("Unexpected member of Relationship enum: \"%s\""
                       , relationship.toString()));
                 }
               }, httpExecutionContext.current());
          }
          else if (controls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
            return journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CATCHALL_CONTROLS_NOT_APPLICABLE_FLOW,
                SoftTechCatchallControlsNotApplicableFlow.RETURN_TO_SOFT_TECH_CATCHALL_CONTROLS);
          }
          else {
            throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
                , controls.toString()));
          }
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> performCatchallSoftTechControlRelationshipTransition(GoodsType goodsType) {
    SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
    return checkRelationshipExists(softTechCategory)
        .thenComposeAsync(relationship ->
                journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CATCHALL_RELATIONSHIP, relationship)
            , httpExecutionContext.current());
  }

  public static CompletionStage<Result> validateThenGetResult(String goodsTypeText, Function<GoodsType, CompletionStage<Result>> resultFunc) {
    if (StringUtils.isNotEmpty(goodsTypeText)) {
      GoodsType goodsType = GoodsType.valueOf(goodsTypeText.toUpperCase());
      if (goodsType == GoodsType.SOFTWARE || goodsType == GoodsType.TECHNOLOGY) {
        return resultFunc.apply(goodsType);
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\""
            , goodsType.toString()));
      }
    }
    else {
      throw new RuntimeException(String.format("Expected goodsTypeText to not be empty"));
    }
  }

  private static CompletionStage<Result> validateGoodsTypeAndGetResult(String goodsTypeText,
                                                                       SoftTechControlsJourney softwareJourney,
                                                                       SoftTechControlsJourney technologyJourney,
                                                                       Function<SoftTechControlsJourney, CompletionStage<Result>> resultFunc) {
    if (StringUtils.isNotEmpty(goodsTypeText)) {
      GoodsType goodsType = GoodsType.valueOf(goodsTypeText.toUpperCase());
      if (goodsType == GoodsType.SOFTWARE) {
        return resultFunc.apply(softwareJourney);
      }
      else if (goodsType == GoodsType.TECHNOLOGY) {
        return resultFunc.apply(technologyJourney);
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\""
            , goodsType.toString()));
      }
    }
    else {
      throw new RuntimeException(String.format("Expected goodsTypeText to not be empty"));
    }
  }

  public static CompletionStage<Result> getCatchallControlsResult(String goodsTypeText, Function<SoftTechControlsJourney, CompletionStage<Result>> resultFunc) {
    return validateGoodsTypeAndGetResult(goodsTypeText, SoftTechControlsJourney.SOFTWARE_CATCHALL,
        SoftTechControlsJourney.TECHNOLOGY_CATCHALL, resultFunc);
  }
}
