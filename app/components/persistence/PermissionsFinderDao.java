package components.persistence;

import com.google.inject.Inject;
import components.common.persistence.CommonRedisDao;
import components.common.persistence.RedisKeyConfig;
import components.common.transaction.TransactionManager;
import controllers.TradeTypeController;
import model.ExportCategory;
import model.TradeType;
import play.libs.Json;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class PermissionsFinderDao extends CommonRedisDao {

  public static final String PHYSICAL_GOOD_CONTROL_CODE = "physicalGoodControlCode";

  public static final String SOURCE_COUNTRY = "sourceCountry";

  public static final String DESTINATION_COUNTRY_LIST = "destinationCountryList";

  public static final String OGEL_ACTIVITY_LIST = "ogelActivityList";

  public static final String OGEL_ID = "ogelId";

  public static final String EXPORT_CATEGORY = "exportCategory";

  public static final String APPLICATION_CODE = "applicationCode";

  public static final String EMAIL_ADDRESS = "emailAddress";

  public static final String MEMORABLE_WORD = "memorableWord";

  public static final String PHYSICAL_GOOD_SEARCH_TERMS = "physicalGoodSearchTerms";

  public static final String PHYSICAL_GOOD_SEARCH_PAGINATION_DISPLAY_COUNT = "physicalGoodSearchPaginationDisplayCount";

  public static final String TRADE_TYPE = "tradeType";

  @Inject
  public PermissionsFinderDao(RedisKeyConfig keyConfig, JedisPool pool, TransactionManager transactionManager) {
    super(keyConfig, pool, transactionManager);
  }

  public void savePhysicalGoodControlCode(String physicalGoodControlCode) {
    writeString(PHYSICAL_GOOD_CONTROL_CODE, physicalGoodControlCode);
  }

  public String getPhysicalGoodControlCode() {
    return readString(PHYSICAL_GOOD_CONTROL_CODE);
  }

  public void saveSourceCountry(String sourceCountry) {
    writeString(SOURCE_COUNTRY, sourceCountry);
  }

  public String getSourceCountry() {
    return readString(SOURCE_COUNTRY);
  }

  public void saveDestinationCountryList(List<String> destinationCountries) {
    writeString(DESTINATION_COUNTRY_LIST, Json.stringify(Json.toJson(destinationCountries)));
  }

  public List<String> getDestinationCountryList() {
    return new LinkedList<String>(Arrays.asList(Json.fromJson(Json.parse(readString(DESTINATION_COUNTRY_LIST)), String[].class)));
  }

  public void saveOgelActivityList(List<String> ogelActivities) {
    writeString(OGEL_ACTIVITY_LIST, Json.stringify(Json.toJson(ogelActivities)));
  }

  public List<String> getOgelActivityList() {
    return Arrays.asList(Json.fromJson(Json.parse(readString(OGEL_ACTIVITY_LIST)), String[].class));
  }

  public void saveOgelId(String ogelId) {
    writeString(OGEL_ID, ogelId);
  }

  public String getOgelId() {
    return readString(OGEL_ID);
  }

  public void saveExportCategory(ExportCategory exportCategory) {
    writeString(EXPORT_CATEGORY, exportCategory.value());
  }

  public ExportCategory getExportCategory() {
    return ExportCategory.getMatched(readString(EXPORT_CATEGORY)).get();
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

  public void saveMemorableWord(String memorableWord) {
    writeString(MEMORABLE_WORD, memorableWord);
  }

  public String getMemorableWord() {
    return readString(MEMORABLE_WORD);
  }

  public void savePhysicalGoodSearchTerms(String physicalGoodSearchTerms) {
    writeString(PHYSICAL_GOOD_SEARCH_TERMS, physicalGoodSearchTerms);
  }

  public String getPhysicalGoodSearchTerms() {
    return readString(PHYSICAL_GOOD_SEARCH_TERMS);
  }

  public void savePhysicalGoodSearchPaginationDisplayCount(int physicalGoodSearchPaginationDisplayCount) {
    writeString(PHYSICAL_GOOD_SEARCH_PAGINATION_DISPLAY_COUNT, Integer.toString(physicalGoodSearchPaginationDisplayCount));
  }

  public int getPhysicalGoodSearchPaginationDisplayCount() {
    return Integer.parseInt(readString(PHYSICAL_GOOD_SEARCH_PAGINATION_DISPLAY_COUNT));
  }

  public void saveTradeType(TradeType tradeType) {
    writeString(TRADE_TYPE, tradeType.value());
  }

  public Optional<TradeType> getTradeType() {
    return TradeType.getMatched(readString(TRADE_TYPE));
  }
}
