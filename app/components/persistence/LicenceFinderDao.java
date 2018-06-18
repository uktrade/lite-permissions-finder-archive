package components.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.persistence.CommonRedisDao;
import components.common.persistence.StatelessRedisDao;
import controllers.licencefinder.QuestionsController;
import models.TradeType;
import models.persistence.RegisterLicence;
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
  private static final String REGISTER_LICENCE = "registerLicence";

  private final CommonRedisDao dao;
  private final StatelessRedisDao statelessRedisDao;

  @Inject
  public LicenceFinderDao(@Named("permissionsFinderDaoHashCommon") CommonRedisDao dao, StatelessRedisDao statelessRedisDao) {
    this.dao = dao;
    this.statelessRedisDao = statelessRedisDao;
  }

  public void saveCustomerId(String sessionId, String arg) {
   // dao.writeString(CUSTOMER_ID, arg);
    statelessRedisDao.writeString(sessionId, CUSTOMER_ID, arg);
  }

  public String getCustomerId(String sessionId) {
    //return dao.readString(CUSTOMER_ID);
    return statelessRedisDao.readString(sessionId, CUSTOMER_ID);
  }

  public void saveSiteId(String sessionId, String arg) {
    //dao.writeString(SITE_ID, arg);
    statelessRedisDao.writeString(sessionId, SITE_ID, arg);
  }

  public String getSiteId(String sessionId) {
    //return dao.readString(SITE_ID);
    return statelessRedisDao.readString(sessionId, SITE_ID);
  }

  public void saveControlCode(String sessionId, String controlCode) {

    //dao.writeString(CONTROL_CODE, controlCode);
    statelessRedisDao.writeString(sessionId, CONTROL_CODE, controlCode);
  }

  public String getControlCode(String sessionId) {
    //return dao.readString(CONTROL_CODE);
    return statelessRedisDao.readString(sessionId, CONTROL_CODE);
  }

  public void saveSourceCountry(String sessionId, String countryCode) {

    //dao.writeString(SOURCE_COUNTRY, countryCode);
    statelessRedisDao.writeString(sessionId, SOURCE_COUNTRY, countryCode);
  }

  public String getSourceCountry(String sessionId) {
    //return dao.readString(SOURCE_COUNTRY);
    return statelessRedisDao.readString(sessionId, SOURCE_COUNTRY);
  }

  public void saveOgelId(String sessionId, String arg) {

    //dao.writeString(OGEL_ID, arg);
    statelessRedisDao.writeString(sessionId, OGEL_ID, arg);
  }

  public String getOgelId(String sessionId) {
    //return dao.readString(OGEL_ID);
    return statelessRedisDao.readString(sessionId, OGEL_ID);
  }

  public void saveDestinationCountry(String sessionId, String arg) {
    //dao.writeString(DESTINATION_COUNTRY, arg);
    statelessRedisDao.writeString(sessionId, DESTINATION_COUNTRY, arg);
  }

  public String getDestinationCountry(String sessionId) {
    //return dao.readString(DESTINATION_COUNTRY);
    return statelessRedisDao.readString(sessionId, DESTINATION_COUNTRY);
  }

  public void saveFirstConsigneeCountry(String sessionId, String arg) {

    //dao.writeString(FIRST_CONSIGNEE_COUNTRY, arg);
    statelessRedisDao.writeString(sessionId, FIRST_CONSIGNEE_COUNTRY, arg);
  }

  public String getFirstConsigneeCountry(String sessionId) {
    //return dao.readString(FIRST_CONSIGNEE_COUNTRY);
    return statelessRedisDao.readString(sessionId, FIRST_CONSIGNEE_COUNTRY);
  }

  public void saveTradeType(String sessionId, TradeType tradeType) {
    //dao.writeString(TRADE_TYPE, tradeType.toString());
    statelessRedisDao.writeString(sessionId, TRADE_TYPE, tradeType.toString());
  }

  public Optional<TradeType> getTradeType(String sessionId) {
    //String tradeType = dao.readString(TRADE_TYPE);
    String tradeType = statelessRedisDao.readString(sessionId, TRADE_TYPE);
    return StringUtils.isBlank(tradeType) ? Optional.empty() : Optional.of(TradeType.valueOf(tradeType));
  }

  public void saveQuestionsForm(String sessionId, QuestionsController.QuestionsForm form) {
    //dao.writeObject(OGEL_QUESTIONS, form);
    statelessRedisDao.writeObject(sessionId, OGEL_QUESTIONS, form);
  }

  public Optional<QuestionsController.QuestionsForm> getQuestionsForm(String sessionId) {
    //return dao.readObject(OGEL_QUESTIONS, QuestionsController.QuestionsForm.class);
    return statelessRedisDao.readObject(sessionId, OGEL_QUESTIONS, QuestionsController.QuestionsForm.class);
  }

  public void saveMultipleCountries(String sessionId, boolean countries) {
    writeBoolean(sessionId, MULTIPLE_COUNTRIES, countries);
  }

  public Optional<Boolean> getMultipleCountries(String sessionId) {
    return readBoolean(sessionId, MULTIPLE_COUNTRIES);
  }

  public void saveUserOgelIdRefMap(String sessionId, Map<String, String> ogelIdRefMap) {
    statelessRedisDao.writeObject(sessionId, USER_OGEL_ID_REF_MAP, ogelIdRefMap);
  }

  public Map<String, String> getUserOgelIdRefMap(String sessionId) {
    return statelessRedisDao.readObject(sessionId, USER_OGEL_ID_REF_MAP, new TypeReference<Map<String, String>>() {})
        .orElse(new HashMap<>());
  }

  public void saveRegisterLicence(String sessionId, RegisterLicence registerLicence) {
    statelessRedisDao.writeObject(sessionId, REGISTER_LICENCE, registerLicence);
  }

  public Optional<RegisterLicence> getRegisterLicence(String sessionId) {
    return statelessRedisDao.readObject(sessionId, REGISTER_LICENCE, RegisterLicence.class);
  }

  /**
   * Private methods
   **/
  private void writeBoolean(String sessionId, String fieldName, boolean value) {
    statelessRedisDao.writeString(sessionId, fieldName, Boolean.toString(value));
  }

  private Optional<Boolean> readBoolean(String sessionId, String fieldName) {
    String value = statelessRedisDao.readString(sessionId, fieldName);
    if (value == null || value.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(value.equalsIgnoreCase("true"));
  }
}
