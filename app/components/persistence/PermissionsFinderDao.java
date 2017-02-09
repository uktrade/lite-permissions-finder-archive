package components.persistence;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.persistence.CommonRedisDao;
import components.common.persistence.RedisKeyConfig;
import components.common.transaction.TransactionManager;
import controllers.categories.ArtsCulturalController.ArtsCulturalForm;
import controllers.ogel.OgelQuestionsController.OgelQuestionsForm;
import controllers.search.SearchController.SearchForm;
import models.ExportCategory;
import models.GoodsType;
import models.LifeType;
import models.TradeType;
import models.controlcode.ControlCodeSubJourney;
import models.softtech.SoftwareExemptionQuestion;
import models.softtech.SoftTechCategory;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class PermissionsFinderDao extends CommonRedisDao {

  public static final String JOURNEY = "journey";
  public static final String SOURCE_COUNTRY = "sourceCountry";
  public static final String SELECTED_CONTROL_CODE = "selectedControlCode";
  public static final String OGEL_ID = "ogelId";
  public static final String EXPORT_CATEGORY = "exportCategory";
  public static final String APPLICATION_CODE = "applicationCode";
  public static final String EMAIL_ADDRESS = "emailAddress";
  public static final String SEARCH_PAGINATION_DISPLAY_COUNT = "searchPaginationDisplayCount";
  public static final String SEARCH_LAST_CHOSEN_CONTROL_CODE = "searchLastChosenControlCode";
  public static final String TRADE_TYPE = "tradeType";
  public static final String ARTS_CULTURAL_GOODS = "artsCulturalGoods";
  public static final String IS_DUAL_USE_GOOD = "isDualUseGood";
  public static final String IS_USED_FOR_EXECUTION_TORTURE = "isUsedForExecutionTorture";
  public static final String PLANTS_ANIMALS_LIFE_TYPE = "plantsAnimalsLifeType";
  public static final String GOODS_TYPE = "goodsType";
  public static final String PHYSICAL_GOOD_SEARCH = "physicalGoodSearch";
  public static final String OGEL_QUESTIONS = "ogelQuestions";
  public static final String OGEL_CONDITIONS_APPLY = "ogelConditionsApply";
  public static final String ITEM_THROUGH_MULTIPLE_COUNTRIES = "itemThroughMultipleCountries";
  public static final String FINAL_DESTINATION_COUNTRY = "finalDestinationCountry";
  public static final String THROUGH_DESTINATION_COUNTRY_LIST = "throughDestinationCountryList";
  public static final String OGEL_REGISTRATION_SERVICE_TRANSACTION_EXISTS = "ogelRegistrationServiceTransactionExists";
  public static final String NON_MILITARY_FIREARMS_EXPORTED_BY_SELF = "nonMilitaryFirearmsExportedBySelf";
  public static final String CONTROL_CODE_APPLIES = "controlCodeAdditionalApplies";
  public static final String CONTROL_CODE_ADDITIONAL_SPECIFICATIONS_APPLY = "controlCodeAdditionalSpecificationsApply";
  public static final String CONTROL_CODE_DECONTROLS_APPLY = "controlCodeDecontrolsApply";
  public static final String CONTROL_CODE_TECHNICAL_NOTES_APPLY = "controlCodeTechnicalNotesApply";
  public static final String SOFT_TECH_CATEGORY = "softTechCategory";
  public static final String RELATED_TO_EQUIPMENT_OR_MATERIALS = "relatedToEquipmentOrMaterials";
  public static final String LAST_STARTED_CONTROL_CODE_SUB_JOURNEY = "lastStartedControlCodeSubJourney";
  public static final String IS_RELATED_TO_GOODS_TYPE = "isRelatedToGoodsType";
  public static final String SOFTWARE_EXEMPTION_QUESTION = "softwareExemptionQuestion";
  public static final String TECHNOLOGY_EXEMPTIONS_APPLY = "technologyExemptionsApply";
  public static final String TECHNOLOGY_IS_NON_EXEMPT = "technologyIsNonExempt";
  public static final String GOODS_RELATIONSHIP_QUESTION_ANSWER = "goodsRelationshipQuestionAnswer";
  public static final String GOODS_RELATIONSHIP_QUESTION_CURRENT_INDEX = "goodsRelationshipQuestionCurrentIndex";
  public static final String CONTROL_CODE_FOR_REGISTRATION = "controlCodeForRegistration";
  public static final String SHOW_TECH_NOTES = "showTechNotes";

  @Inject
  public PermissionsFinderDao(@Named("permissionsFinderDaoHash") RedisKeyConfig keyConfig, JedisPool pool, TransactionManager transactionManager) {
    super(keyConfig, pool, transactionManager);
  }

  public void saveSelectedControlCode(ControlCodeSubJourney controlCodeSubJourney, String selectedControlCode) {
    saveLastStartedControlCodeSubJourney(controlCodeSubJourney);
    writeString(prependFieldName(controlCodeSubJourney, SELECTED_CONTROL_CODE), selectedControlCode);
  }

  public String getSelectedControlCode(ControlCodeSubJourney controlCodeSubJourney) {
    return readString(prependFieldName(controlCodeSubJourney, SELECTED_CONTROL_CODE));
  }

  private void saveLastStartedControlCodeSubJourney(ControlCodeSubJourney lastStartedControlCodeSubJourney) {
    writeString(LAST_STARTED_CONTROL_CODE_SUB_JOURNEY, lastStartedControlCodeSubJourney.value());
  }

  public void saveControlCodeForRegistration(String controlCode){
    writeString(CONTROL_CODE_FOR_REGISTRATION, controlCode);
  }

  public String getControlCodeForRegistration(){
    return readString(CONTROL_CODE_FOR_REGISTRATION);
  }


  public void saveSourceCountry(String sourceCountry) {
    writeString(SOURCE_COUNTRY, sourceCountry);
  }

  public String getSourceCountry() {
    return readString(SOURCE_COUNTRY);
  }

  public void saveOgelId(String ogelId) {
    writeString(OGEL_ID, ogelId);
  }

  public String getOgelId() {
    return readString(OGEL_ID);
  }

  public void saveExportCategory(ExportCategory exportCategory) {
    writeString(EXPORT_CATEGORY, exportCategory.toString());
  }

  public Optional<ExportCategory> getExportCategory() {
    return ExportCategory.getMatched(readString(EXPORT_CATEGORY));
  }

  public void saveApplicationCode(String applicationCode) {
    writeString(APPLICATION_CODE, applicationCode);
  }

  public String getApplicationCode() {
    return readString(APPLICATION_CODE);
  }

  public void saveEmailAddress(String emailAddress) {
    writeString(EMAIL_ADDRESS, emailAddress);
  }

  public String getEmailAddress() {
    return readString(EMAIL_ADDRESS);
  }


  public void saveSearchResultsPaginationDisplayCount(ControlCodeSubJourney controlCodeSubJourney, int searchPaginationDisplayCount) {
    saveSearchPaginationDisplayCount("results", controlCodeSubJourney, searchPaginationDisplayCount);
  }

  public void saveSearchRelatedCodesPaginationDisplayCount(ControlCodeSubJourney controlCodeSubJourney, int searchPaginationDisplayCount) {
    saveSearchPaginationDisplayCount("relatedCodes", controlCodeSubJourney, searchPaginationDisplayCount);
  }

  private void saveSearchPaginationDisplayCount(String searchType, ControlCodeSubJourney controlCodeSubJourney, int searchPaginationDisplayCount) {
    writeString(prependFieldName(controlCodeSubJourney, SEARCH_PAGINATION_DISPLAY_COUNT + ":" + searchType), Integer.toString(searchPaginationDisplayCount));
  }

  public Optional<Integer> getSearchResultsPaginationDisplayCount(ControlCodeSubJourney controlCodeSubJourney) {
    return getSearchPaginationDisplayCount("results", controlCodeSubJourney);
  }

  public Optional<Integer> getSearchRelatedCodesPaginationDisplayCount(ControlCodeSubJourney controlCodeSubJourney) {
    return getSearchPaginationDisplayCount("relatedCodes", controlCodeSubJourney);
  }

  private Optional<Integer> getSearchPaginationDisplayCount(String searchType, ControlCodeSubJourney controlCodeSubJourney) {
    String count = readString(prependFieldName(controlCodeSubJourney, SEARCH_PAGINATION_DISPLAY_COUNT + ":" + searchType));
    if (count != null) {
      return Optional.of(Integer.parseInt(count));
    }
    else {
      return Optional.empty();
    }
  }

  public void saveSearchResultsLastChosenControlCode(ControlCodeSubJourney controlCodeSubJourney, String controlCode) {
    saveSearchLastChosenControlCode("results", controlCodeSubJourney, controlCode);
  }

  public void saveSearchRelatedCodesLastChosenControlCode(ControlCodeSubJourney controlCodeSubJourney, String controlCode) {
    saveSearchLastChosenControlCode("relatedCodes", controlCodeSubJourney, controlCode);
  }

  private void saveSearchLastChosenControlCode(String searchType, ControlCodeSubJourney controlCodeSubJourney, String controlCode) {
    writeString(prependFieldName(controlCodeSubJourney, SEARCH_LAST_CHOSEN_CONTROL_CODE + ":" + searchType), controlCode);
  }

  public String getSearchResultsLastChosenControlCode(ControlCodeSubJourney controlCodeSubJourney) {
    return getSearchLastChosenControlCode("results", controlCodeSubJourney);
  }

  public String getSearchRelatedCodesLastChosenControlCode(ControlCodeSubJourney controlCodeSubJourney) {
    return getSearchLastChosenControlCode("relatedCodes", controlCodeSubJourney);
  }

  private String getSearchLastChosenControlCode(String searchType, ControlCodeSubJourney controlCodeSubJourney) {
    return readString(prependFieldName(controlCodeSubJourney, SEARCH_LAST_CHOSEN_CONTROL_CODE + ":" + searchType));
  }

  public void clearSearchResultsLastChosenControlCode(ControlCodeSubJourney controlCodeSubJourney) {
    clearSearchLastChosenControlCode("results", controlCodeSubJourney);
  }

  public void clearSearchRelatedCodesLastChosenControlCode(ControlCodeSubJourney controlCodeSubJourney) {
    clearSearchLastChosenControlCode("relatedCodes", controlCodeSubJourney);
  }

  private void clearSearchLastChosenControlCode(String searchType, ControlCodeSubJourney controlCodeSubJourney) {
    deleteString(prependFieldName(controlCodeSubJourney, SEARCH_LAST_CHOSEN_CONTROL_CODE + ":" + searchType));
  }

  public void saveTradeType(TradeType tradeType) {
    writeString(TRADE_TYPE, tradeType.toString());
  }

  public Optional<TradeType> getTradeType() {
    String tradeType = readString(TRADE_TYPE);
    return StringUtils.isBlank(tradeType) ? Optional.empty() : Optional.of(TradeType.valueOf(tradeType));
  }

  public void saveArtsCulturalForm (ArtsCulturalForm form) {
    writeObject(ARTS_CULTURAL_GOODS, form);
  }

  public Optional<ArtsCulturalForm> getArtsCulturalForm() {
    return readObject(ARTS_CULTURAL_GOODS, ArtsCulturalForm.class);
  }

  public void saveIsDualUseGood(boolean isDualUseGood) {
    writeBoolean(IS_DUAL_USE_GOOD, isDualUseGood);
  }

  public Optional<Boolean> getIsDualUseGood() {
    return readBoolean(IS_DUAL_USE_GOOD);
  }

  public void saveIsUsedForExecutionTorture(boolean isUsedForExecutionTorture) {
    writeBoolean(IS_USED_FOR_EXECUTION_TORTURE, isUsedForExecutionTorture);
  }

  public Optional<Boolean> getIsUsedForExecutionTorture() {
    return readBoolean(IS_USED_FOR_EXECUTION_TORTURE);
  }

  public void writeBoolean(String fieldName, boolean value){
    writeString(fieldName, Boolean.toString(value));
  }

  public Optional<Boolean> readBoolean(String fieldName) {
    String value = readString(fieldName);
    if (value == null || value.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(value.equalsIgnoreCase("true"));
  }

  public void savePlantsAnimalsLifeType(LifeType lifeType) {
    writeString(PLANTS_ANIMALS_LIFE_TYPE, lifeType.value());
  }

  public Optional<LifeType> getPlantsAnimalsLifeType() {
    return LifeType.getMatched(readString(PLANTS_ANIMALS_LIFE_TYPE));
  }

  public void saveGoodsType(GoodsType goodsType) {
    writeString(GOODS_TYPE, goodsType.value());
  }

  public Optional<GoodsType> getGoodsType() {
    return GoodsType.getMatchedByValue(readString(GOODS_TYPE));
  }

  public void clearGoodsType() {
    deleteString(GOODS_TYPE);
  }

  public void savePhysicalGoodSearchForm(ControlCodeSubJourney controlCodeSubJourney, SearchForm searchForm) {
    writeObject(prependFieldName(controlCodeSubJourney, PHYSICAL_GOOD_SEARCH), searchForm);
  }

  public Optional<SearchForm> getPhysicalGoodsSearchForm(ControlCodeSubJourney controlCodeSubJourney) {
    return readObject(prependFieldName(controlCodeSubJourney, PHYSICAL_GOOD_SEARCH), SearchForm.class);
  }

  public void saveOgelQuestionsForm(OgelQuestionsForm ogelQuestionsForm) {
    writeObject(OGEL_QUESTIONS, ogelQuestionsForm);
  }

  public Optional<OgelQuestionsForm> getOgelQuestionsForm() {
    return readObject(OGEL_QUESTIONS, OgelQuestionsForm.class);
  }

  public void saveOgelConditionsApply(boolean ogelConditionsApply) {
    writeBoolean(OGEL_CONDITIONS_APPLY, ogelConditionsApply);
  }

  public Optional<Boolean> getOgelConditionsApply() {
    return readBoolean(OGEL_CONDITIONS_APPLY);
  }

  public void saveItemThroughMultipleCountries (boolean itemThroughMultipleCountries) {
    writeBoolean(ITEM_THROUGH_MULTIPLE_COUNTRIES, itemThroughMultipleCountries);
  }

  public Optional<Boolean> getItemThroughMultipleCountries() {
    return readBoolean(ITEM_THROUGH_MULTIPLE_COUNTRIES);
  }

  public void saveFinalDestinationCountry(String finalDestinationCountry) {
    writeString(FINAL_DESTINATION_COUNTRY, finalDestinationCountry);
  }

  public String getFinalDestinationCountry() {
    return readString(FINAL_DESTINATION_COUNTRY);
  }

  public void saveThroughDestinationCountries(List<String> throughDestinationCountries) {
    writeObject(THROUGH_DESTINATION_COUNTRY_LIST, throughDestinationCountries);
  }

  public List<String> getThroughDestinationCountries() {
    String countriesJson = readString(THROUGH_DESTINATION_COUNTRY_LIST);
    if (countriesJson == null || countriesJson.isEmpty()) {
      return Collections.emptyList();
    }
    else {
      return new LinkedList<>(Arrays.asList(Json.fromJson(Json.parse(countriesJson), String[].class)));
    }
  }

  public String readJourneyString() {
    return readString(JOURNEY);
  }

  public void writeJourneyString(String journeyString) {
    writeString(JOURNEY, journeyString);
  }

  public void saveOgelRegistrationServiceTransactionExists (boolean transactionCreated) {
    writeBoolean(OGEL_REGISTRATION_SERVICE_TRANSACTION_EXISTS, transactionCreated);
  }

  public Optional<Boolean> getOgelRegistrationServiceTransactionExists() {
    return readBoolean(OGEL_REGISTRATION_SERVICE_TRANSACTION_EXISTS);
  }

  public void saveNonMilitaryFirearmsExportedBySelf(String nonMilitaryFirearmsExportedBySelf) {
    writeString(NON_MILITARY_FIREARMS_EXPORTED_BY_SELF, nonMilitaryFirearmsExportedBySelf);
  }

  public String readNonMilitaryFirearmsExportedBySelf() {
    return readString(NON_MILITARY_FIREARMS_EXPORTED_BY_SELF);
  }

  public void saveControlCodeApplies(ControlCodeSubJourney controlCodeSubJourney, boolean controlCodeApplies) {
    writeBoolean(prependFieldName(controlCodeSubJourney, CONTROL_CODE_APPLIES), controlCodeApplies);
  }

  public Optional<Boolean> getControlCodeApplies(ControlCodeSubJourney controlCodeSubJourney) {
    return readBoolean(prependFieldName(controlCodeSubJourney, CONTROL_CODE_APPLIES));
  }

  public void clearControlCodeApplies(ControlCodeSubJourney controlCodeSubJourney) {
    deleteString(prependFieldName(controlCodeSubJourney, CONTROL_CODE_APPLIES));
  }

  public void saveControlCodeDecontrolsApply(ControlCodeSubJourney controlCodeSubJourney, boolean decontrolsApply) {
    writeBoolean(prependFieldName(controlCodeSubJourney, CONTROL_CODE_DECONTROLS_APPLY), decontrolsApply);
  }

  public Optional<Boolean> getControlCodeDecontrolsApply(ControlCodeSubJourney controlCodeSubJourney) {
    return readBoolean(prependFieldName(controlCodeSubJourney, CONTROL_CODE_DECONTROLS_APPLY));
  }

  public void clearControlCodeDecontrolsApply(ControlCodeSubJourney controlCodeSubJourney) {
    deleteString(prependFieldName(controlCodeSubJourney, CONTROL_CODE_DECONTROLS_APPLY));
  }

  public void saveControlCodeAdditionalSpecificationsApply(ControlCodeSubJourney controlCodeSubJourney, boolean additionalSpecificationsApply) {
    writeBoolean(prependFieldName(controlCodeSubJourney, CONTROL_CODE_ADDITIONAL_SPECIFICATIONS_APPLY), additionalSpecificationsApply);
  }

  public Optional<Boolean> getControlCodeAdditionalSpecificationsApply(ControlCodeSubJourney controlCodeSubJourney) {
    return readBoolean(prependFieldName(controlCodeSubJourney, CONTROL_CODE_ADDITIONAL_SPECIFICATIONS_APPLY));
  }

  public void clearControlCodeAdditionalSpecificationsApply(ControlCodeSubJourney controlCodeSubJourney) {
    deleteString(prependFieldName(controlCodeSubJourney, CONTROL_CODE_ADDITIONAL_SPECIFICATIONS_APPLY));
  }

  public void saveControlCodeTechnicalNotesApply(ControlCodeSubJourney controlCodeSubJourney, boolean technicalNotesApply) {
    writeBoolean(prependFieldName(controlCodeSubJourney, CONTROL_CODE_TECHNICAL_NOTES_APPLY), technicalNotesApply);
  }

  public Optional<Boolean> getControlCodeTechnicalNotesApply(ControlCodeSubJourney controlCodeSubJourney) {
    return readBoolean(prependFieldName(controlCodeSubJourney, CONTROL_CODE_TECHNICAL_NOTES_APPLY));
  }

  public void clearControlCodeTechnicalNotesApply(ControlCodeSubJourney controlCodeSubJourney) {
    deleteString(prependFieldName(controlCodeSubJourney, CONTROL_CODE_TECHNICAL_NOTES_APPLY));
  }

  public void saveSoftTechCategory(GoodsType goodsType, SoftTechCategory softTechCategory) {
    writeString(prependFieldName(goodsType, SOFT_TECH_CATEGORY), softTechCategory.toString());
  }

  public Optional<SoftTechCategory> getSoftTechCategory(GoodsType goodsType) {
    String softwareCategory = readString(prependFieldName(goodsType, SOFT_TECH_CATEGORY));
    if (StringUtils.isEmpty(softwareCategory)) {
      return Optional.empty();
    }
    else {
      try {
        return Optional.of(SoftTechCategory.valueOf(softwareCategory));
      }
      catch (IllegalArgumentException e) {
        return Optional.empty();
      }
    }
  }

  public void saveRelatedToEquipmentOrMaterials(GoodsType goodsType, Boolean relatedToEquipmentOrMaterials) {
    writeString(prependFieldName(goodsType.urlString(), RELATED_TO_EQUIPMENT_OR_MATERIALS), relatedToEquipmentOrMaterials.toString());
  }

  public Optional<Boolean> getRelatedToEquipmentOrMaterials(GoodsType goodsType) {
    return readBoolean(prependFieldName(goodsType.urlString(), RELATED_TO_EQUIPMENT_OR_MATERIALS));
  }

  public String prependFieldName(ControlCodeSubJourney controlCodeSubJourney, String fieldName) {
    return prependFieldName(controlCodeSubJourney.value(), fieldName);
  }

  public String prependFieldName(GoodsType goodsType, String fieldName) {
    return prependFieldName(goodsType.urlString(), fieldName);
  }

  public String prependFieldName(String prefix, String fieldName) {
    return prefix + ":" + fieldName;
  }

  public void clearControlCodeSubJourneyDaoFields(ControlCodeSubJourney controlCodeSubJourney) {
    clearControlCodeApplies(controlCodeSubJourney);
    clearControlCodeDecontrolsApply(controlCodeSubJourney);
    clearControlCodeAdditionalSpecificationsApply(controlCodeSubJourney);
    clearControlCodeTechnicalNotesApply(controlCodeSubJourney);
  }

  public void clearAndUpdateControlCodeSubJourneyDaoFieldsIfChanged(ControlCodeSubJourney controlCodeSubJourney, String newSelectedControlCode) {
    String oldSelectedControlCode = getSelectedControlCode(controlCodeSubJourney);
    if (!StringUtils.equals(newSelectedControlCode, oldSelectedControlCode)) {
      clearControlCodeSubJourneyDaoFields(controlCodeSubJourney);
      saveSelectedControlCode(controlCodeSubJourney,newSelectedControlCode);
    }
  }

  public void saveIsRelatedToGoodsType(GoodsType goodsType, GoodsType relatedToGoodsType, boolean answer) {
    writeBoolean(prependFieldName(goodsType, prependFieldName(relatedToGoodsType, IS_RELATED_TO_GOODS_TYPE)), answer);
  }

  public Optional<Boolean> getIsRelatedToGoodsType (GoodsType goodsType, GoodsType relatedToGoodsType) {
    return readBoolean(prependFieldName(goodsType, prependFieldName(relatedToGoodsType, IS_RELATED_TO_GOODS_TYPE)));
  }

  public void saveSoftwareExemptionQuestion(SoftwareExemptionQuestion softwareExemptionQuestion, boolean doExemptionsApply) {
    writeBoolean(SOFTWARE_EXEMPTION_QUESTION + ":" + softwareExemptionQuestion.toString(), doExemptionsApply);
  }

  public Optional<Boolean> getSoftwareExemptionQuestion(SoftwareExemptionQuestion softwareExemptionQuestion) {
    return readBoolean(SOFTWARE_EXEMPTION_QUESTION + ":" + softwareExemptionQuestion.toString());
  }

  public void saveTechnologyExemptionsApply(boolean doExemptionsApply) {
    writeBoolean(TECHNOLOGY_EXEMPTIONS_APPLY, doExemptionsApply);
  }

  public Optional<Boolean> getTechnologyExemptionsApply() {
    return readBoolean(TECHNOLOGY_EXEMPTIONS_APPLY);
  }

  public void saveGoodsRelationshipQuestionAnswer(GoodsType goodsType, GoodsType relatedToGoodsType, int questionIndex, boolean answer) {
    writeBoolean(goodsRelationshipFieldNamePrefix(goodsType, relatedToGoodsType) + ":" +  GOODS_RELATIONSHIP_QUESTION_ANSWER + ":" + Integer.toString(questionIndex), answer);
  }

  public Optional<Boolean> getGoodsRelationshipQuestionAnswer (GoodsType goodsType, GoodsType relatedToGoodsType, int questionIndex) {
    return readBoolean(goodsRelationshipFieldNamePrefix(goodsType, relatedToGoodsType) + ":" +  GOODS_RELATIONSHIP_QUESTION_ANSWER + ":" + Integer.toString(questionIndex));
  }

  public void saveGoodsRelationshipQuestionCurrentIndex(GoodsType goodsType, GoodsType relatedToGoodsType, int currentQuestionIndex) {
    writeString(prependFieldName(goodsType, prependFieldName(relatedToGoodsType, GOODS_RELATIONSHIP_QUESTION_CURRENT_INDEX)), Integer.toString(currentQuestionIndex));
  }

  public Optional<Integer> getGoodsRelationshipQuestionCurrentIndex (GoodsType goodsType, GoodsType relatedToGoodsType) {
    String currentQuestionIndexString = readString(prependFieldName(goodsType, prependFieldName(relatedToGoodsType, GOODS_RELATIONSHIP_QUESTION_CURRENT_INDEX)));
    if (StringUtils.isNotEmpty(currentQuestionIndexString)) {
      return Optional.of(Integer.parseInt(currentQuestionIndexString));
    }
    else {
      return Optional.empty();
    }
  }

  private String goodsRelationshipFieldNamePrefix(GoodsType goodsType, GoodsType relatedToGoodsType) {
    return goodsType.urlString() + ":" + relatedToGoodsType.urlString();
  }

  public void saveTechnologyIsNonExempt(boolean isNonExempt) {
    writeBoolean(TECHNOLOGY_IS_NON_EXEMPT, isNonExempt);
  }

  public Optional<Boolean> getTechnologyIsNonExempt() {
    return readBoolean(TECHNOLOGY_IS_NON_EXEMPT);
  }

  public void saveShowTechNotes(ControlCodeSubJourney controlCodeSubJourney, String controlCode, boolean showTechNotes) {
    writeBoolean(SHOW_TECH_NOTES + ":" + controlCodeSubJourney.value() + ":" + controlCode, showTechNotes);
  }

  public Optional<Boolean> getShowTechNotes(ControlCodeSubJourney controlCodeSubJourney, String controlCode) {
    return readBoolean(SHOW_TECH_NOTES + ":" + controlCodeSubJourney.value() + ":" + controlCode);
  }

}
