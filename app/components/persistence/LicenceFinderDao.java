package components.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.persistence.CommonRedisDao;
import controllers.licencefinder.QuestionsController;
import models.TradeType;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LicenceFinderDao {

  private static final String CONTROL_CODE = "controlCode";
  private static final String SOURCE_COUNTRY = "sourceCountry";
  private static final String OGEL_ID = "ogelId";
  private static final String CUSTOMER_ID = "customerId";
  private static final String SITE_ID = "siteId";
  private static final String TRADE_TYPE = "tradeType";
  private static final String OGEL_QUESTIONS = "ogelQuestions";
  private static final String DESTINATION_COUNTRY = "destinationCountry";
  private static final String FIRST_CONSIGNEE_COUNTRY = "firstConsigneeCountry";
  private static final String MULTIPLE_COUNTRIES = "multipleCountries";
  private static final String USER_OGEL_ID_REF_MAP = "userOgelIdRefMap";

  private final CommonRedisDao dao;

  @Inject
  public LicenceFinderDao(@Named("permissionsFinderDaoHashCommon") CommonRedisDao dao) {
    this.dao = dao;
  }

  public void saveCustomerId(String arg) {
    dao.writeString(CUSTOMER_ID, arg);
  }

  public String getCustomerId() {
    return dao.readString(CUSTOMER_ID);
  }

  public void saveSiteId(String arg) {
    dao.writeString(SITE_ID, arg);
  }

  public String getSiteId() {
    return dao.readString(SITE_ID);
  }

  public void saveControlCode(String sessionId, String controlCode) {
    dao.writeString(CONTROL_CODE, controlCode);
  }

  public String getControlCode(String sessionId) {
    return dao.readString(CONTROL_CODE);
  }

  public void saveSourceCountry(String sessionId, String countryCode) {
    dao.writeString(SOURCE_COUNTRY, countryCode);
  }

  public String getSourceCountry(String sessionId) {
    return dao.readString(SOURCE_COUNTRY);
  }

  public void saveOgelId(String arg) {
    dao.writeString(OGEL_ID, arg);
  }

  public String getOgelId() {
    return dao.readString(OGEL_ID);
  }

  public void saveDestinationCountry(String arg) {
    dao.writeString(DESTINATION_COUNTRY, arg);
  }

  public String getDestinationCountry() {
    return dao.readString(DESTINATION_COUNTRY);
  }

  public void saveFirstConsigneeCountry(String arg) {
    dao.writeString(FIRST_CONSIGNEE_COUNTRY, arg);
  }

  public String getFirstConsigneeCountry() {
    return dao.readString(FIRST_CONSIGNEE_COUNTRY);
  }

  public void saveTradeType(String sessionId, TradeType tradeType) {
    dao.writeString(TRADE_TYPE, tradeType.toString());
  }

  public Optional<TradeType> getTradeType(String sessionId) {
    String tradeType = dao.readString(TRADE_TYPE);
    return StringUtils.isBlank(tradeType) ? Optional.empty() : Optional.of(TradeType.valueOf(tradeType));
  }

  public void saveQuestionsForm(QuestionsController.QuestionsForm form) {
    dao.writeObject(OGEL_QUESTIONS, form);
  }

  public Optional<QuestionsController.QuestionsForm> getQuestionsForm() {
    return dao.readObject(OGEL_QUESTIONS, QuestionsController.QuestionsForm.class);
  }

  public void saveMultipleCountries(boolean countries) {
    writeBoolean(MULTIPLE_COUNTRIES, countries);
  }

  public Optional<Boolean> getMultipleCountries() {
    return readBoolean(MULTIPLE_COUNTRIES);
  }

  public void saveUserOgelIdRefMap(Map<String, String> ogelIdRefMap) {
    dao.writeObject(USER_OGEL_ID_REF_MAP, ogelIdRefMap);
  }

  public Map<String, String>getUserOgelIdRefMap() {
    return dao.readObject(USER_OGEL_ID_REF_MAP, new TypeReference<Map<String, String>>() {})
        .orElse(new HashMap<>());
  }

  /**
   * Private methods
   **/
  private void writeBoolean(String fieldName, boolean value) {
    dao.writeString(fieldName, Boolean.toString(value));
  }

  private Optional<Boolean> readBoolean(String fieldName) {
    String value = dao.readString(fieldName);
    if (value == null || value.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(value.equalsIgnoreCase("true"));
  }
}
