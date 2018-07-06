package components.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import components.common.persistence.StatelessRedisDao;
import controllers.licencefinder.QuestionsController;
import models.TradeType;
import models.persistence.RegisterLicence;
import models.view.licencefinder.Customer;
import models.view.licencefinder.Site;
import org.redisson.client.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.EnumUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LicenceFinderDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenceFinderDao.class);

  private static final String CONTROL_CODE = "controlCode";
  private static final String RESUME_CODE = "resumeCode";
  private static final String USER_ID = "userId";
  private static final String SOURCE_COUNTRY = "sourceCountry";
  private static final String OGEL_ID = "ogelId";
  private static final String TRADE_TYPE = "tradeType";
  private static final String OGEL_QUESTIONS = "ogelQuestions";
  private static final String DESTINATION_COUNTRY = "destinationCountry";
  private static final String FIRST_CONSIGNEE_COUNTRY = "firstConsigneeCountry";
  private static final String MULTIPLE_COUNTRIES = "multipleCountries";
  private static final String USER_OGEL_ID_REF_MAP = "userOgelIdRefMap";
  private static final String REGISTER_LICENCE = "registerLicence";
  private static final String CUSTOMER = "customer";
  private static final String SITE = "site";

  private final StatelessRedisDao statelessRedisDao;

  @Inject
  public LicenceFinderDao(StatelessRedisDao statelessRedisDao) {
    this.statelessRedisDao = statelessRedisDao;
  }

  public void saveCustomer(String sessionId, Customer customer) {
    statelessRedisDao.writeObject(sessionId, CUSTOMER, customer);
  }

  public Optional<Customer> getCustomer(String sessionId) {
    return statelessRedisDao.readObject(sessionId, CUSTOMER, Customer.class);
  }

  public void saveSite(String sessionId, Site site) {
    statelessRedisDao.writeObject(sessionId, SITE, site);
  }

  public Optional<Site> getSite(String sessionId) {
    return statelessRedisDao.readObject(sessionId, SITE, Site.class);
  }

  public void saveControlCode(String sessionId, String controlCode) {
    statelessRedisDao.writeString(sessionId, CONTROL_CODE, controlCode);
  }

  public Optional<String> getControlCode(String sessionId) {
    return Optional.ofNullable(statelessRedisDao.readString(sessionId, CONTROL_CODE));
  }

  public void saveResumeCode(String sessionId, String resumeCode) {
    statelessRedisDao.writeString(sessionId, RESUME_CODE, resumeCode);
  }

  public Optional<String> getResumeCode(String sessionId) {
    return Optional.ofNullable(statelessRedisDao.readString(sessionId, RESUME_CODE));
  }

  public void saveUserId(String sessionId, String userId) {
    statelessRedisDao.writeString(sessionId, USER_ID, userId);
  }

  public Optional<String> getUserId(String sessionId) {
    return Optional.ofNullable(statelessRedisDao.readString(sessionId, USER_ID));
  }

  public void saveSourceCountry(String sessionId, String countryCode) {
    statelessRedisDao.writeString(sessionId, SOURCE_COUNTRY, countryCode);
  }

  public Optional<String> getSourceCountry(String sessionId) {
    return Optional.ofNullable(statelessRedisDao.readString(sessionId, SOURCE_COUNTRY));
  }

  public void saveOgelId(String sessionId, String ogelId) {
    statelessRedisDao.writeString(sessionId, OGEL_ID, ogelId);
  }

  public Optional<String> getOgelId(String sessionId) {
    return Optional.ofNullable(statelessRedisDao.readString(sessionId, OGEL_ID));
  }

  public void saveDestinationCountry(String sessionId, String countryCode) {
    statelessRedisDao.writeString(sessionId, DESTINATION_COUNTRY, countryCode);
  }

  public Optional<String> getDestinationCountry(String sessionId) {
    return Optional.ofNullable(statelessRedisDao.readString(sessionId, DESTINATION_COUNTRY));
  }

  public void saveFirstConsigneeCountry(String sessionId, String countryCode) {
    statelessRedisDao.writeString(sessionId, FIRST_CONSIGNEE_COUNTRY, countryCode);
  }

  public Optional<String> getFirstConsigneeCountry(String sessionId) {
    return Optional.ofNullable(statelessRedisDao.readString(sessionId, FIRST_CONSIGNEE_COUNTRY));
  }

  public void saveTradeType(String sessionId, TradeType tradeType) {
    statelessRedisDao.writeString(sessionId, TRADE_TYPE, tradeType.toString());
  }

  public Optional<TradeType> getTradeType(String sessionId) {
    String tradeType = statelessRedisDao.readString(sessionId, TRADE_TYPE);
    return Optional.ofNullable(EnumUtil.parse(tradeType, TradeType.class));
  }

  public void saveQuestionsForm(String sessionId, QuestionsController.QuestionsForm form) {
    statelessRedisDao.writeObject(sessionId, OGEL_QUESTIONS, form);
  }

  public Optional<QuestionsController.QuestionsForm> getQuestionsForm(String sessionId) {
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

  public Map<String, String> getUserOgelIdReferenceMap(String sessionId) {
    return statelessRedisDao.readObject(sessionId, USER_OGEL_ID_REF_MAP,
        new TypeReference<Map<String, String>>() {
        }).orElse(new HashMap<>());
  }

  public void saveRegisterLicence(String sessionId, RegisterLicence registerLicence) {
    statelessRedisDao.writeObject(sessionId, REGISTER_LICENCE, registerLicence);
  }

  public Optional<RegisterLicence> getRegisterLicence(String sessionId) {
    try {
      return statelessRedisDao.readObject(sessionId, REGISTER_LICENCE, RegisterLicence.class);
    } catch (RedisException exception) {
      LOGGER.error("Unable to get registerLicence", exception);
      return Optional.empty();
    }
  }

  private void writeBoolean(String sessionId, String fieldName, boolean value) {
    statelessRedisDao.writeString(sessionId, fieldName, Boolean.toString(value));
  }

  private Optional<Boolean> readBoolean(String sessionId, String fieldName) {
    String value = statelessRedisDao.readString(sessionId, fieldName);
    return Optional.of(Boolean.parseBoolean(value));
  }
}
