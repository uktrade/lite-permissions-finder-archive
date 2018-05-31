package components.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.persistence.CommonRedisDao;
import controllers.ogel.OgelQuestionsController.OgelQuestionsForm;
import models.TradeType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PermissionsFinderDao {

  public static final String JOURNEY = "journey";
  public static final String SOURCE_COUNTRY = "sourceCountry";
  public static final String OGEL_ID = "ogelId";
  public static final String APPLICATION_CODE = "applicationCode";
  public static final String EMAIL_ADDRESS = "emailAddress";
  public static final String TRADE_TYPE = "tradeType";
  public static final String OGEL_QUESTIONS = "ogelQuestions";
  public static final String OGEL_CONDITIONS_APPLY = "ogelConditionsApply";
  public static final String MULTIPLE_COUNTRIES = "multipleCountries";
  public static final String FINAL_DESTINATION_COUNTRY = "finalDestinationCountry";
  public static final String THROUGH_DESTINATION_COUNTRY_LIST = "throughDestinationCountryList";
  public static final String OGEL_REGISTRATION_SERVICE_TRANSACTION_EXISTS = "ogelRegistrationServiceTransactionExists";
  public static final String CONTROL_CODE_FOR_REGISTRATION = "controlCodeForRegistration";

  private final CommonRedisDao commonRedisDao;

  @Inject
  public PermissionsFinderDao(@Named("permissionsFinderDaoHashCommon") CommonRedisDao commonRedisDao) {
    this.commonRedisDao = commonRedisDao;
  }

  public void saveControlCodeForRegistration(String controlCode) {
    commonRedisDao.writeString(CONTROL_CODE_FOR_REGISTRATION, controlCode);
  }

  public String getControlCodeForRegistration() {
    return commonRedisDao.readString(CONTROL_CODE_FOR_REGISTRATION);
  }


  public void saveSourceCountry(String sourceCountry) {
    commonRedisDao.writeString(SOURCE_COUNTRY, sourceCountry);
  }

  public String getSourceCountry() {
    return commonRedisDao.readString(SOURCE_COUNTRY);
  }

  public void saveOgelId(String ogelId) {
    commonRedisDao.writeString(OGEL_ID, ogelId);
  }

  public String getOgelId() {
    return commonRedisDao.readString(OGEL_ID);
  }

  public void saveApplicationCode(String applicationCode) {
    commonRedisDao.writeString(APPLICATION_CODE, applicationCode);
  }

  public String getApplicationCode() {
    return commonRedisDao.readString(APPLICATION_CODE);
  }

  public void saveEmailAddress(String emailAddress) {
    commonRedisDao.writeString(EMAIL_ADDRESS, emailAddress);
  }


  public void saveTradeType(TradeType tradeType) {
    commonRedisDao.writeString(TRADE_TYPE, tradeType.toString());
  }

  public Optional<TradeType> getTradeType() {
    String tradeType = commonRedisDao.readString(TRADE_TYPE);
    return StringUtils.isBlank(tradeType) ? Optional.empty() : Optional.of(TradeType.valueOf(tradeType));
  }

  public void writeBoolean(String fieldName, boolean value) {
    commonRedisDao.writeString(fieldName, Boolean.toString(value));
  }

  public Optional<Boolean> readBoolean(String fieldName) {
    String value = commonRedisDao.readString(fieldName);
    if (value == null || value.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(value.equalsIgnoreCase("true"));
  }

  public void saveOgelQuestionsForm(OgelQuestionsForm ogelQuestionsForm) {
    commonRedisDao.writeObject(OGEL_QUESTIONS, ogelQuestionsForm);
  }

  public Optional<OgelQuestionsForm> getOgelQuestionsForm() {
    return commonRedisDao.readObject(OGEL_QUESTIONS, OgelQuestionsForm.class);
  }

  public void saveOgelConditionsApply(boolean ogelConditionsApply) {
    writeBoolean(OGEL_CONDITIONS_APPLY, ogelConditionsApply);
  }

  public Optional<Boolean> getOgelConditionsApply() {
    return readBoolean(OGEL_CONDITIONS_APPLY);
  }

  public void saveMultipleCountries(boolean multipleCountries) {
    writeBoolean(MULTIPLE_COUNTRIES, multipleCountries);
  }

  public Optional<Boolean> getMultipleCountries() {
    return readBoolean(MULTIPLE_COUNTRIES);
  }

  public void saveFinalDestinationCountry(String finalDestinationCountry) {
    commonRedisDao.writeString(FINAL_DESTINATION_COUNTRY, finalDestinationCountry);
  }

  public String getFinalDestinationCountry() {
    return commonRedisDao.readString(FINAL_DESTINATION_COUNTRY);
  }

  public void saveThroughDestinationCountries(List<String> throughDestinationCountries) {
    commonRedisDao.writeObject(THROUGH_DESTINATION_COUNTRY_LIST, throughDestinationCountries);
  }

  public List<String> getThroughDestinationCountries() {
    return commonRedisDao.readObject(THROUGH_DESTINATION_COUNTRY_LIST, new TypeReference<List<String>>() {})
        .orElse(new ArrayList<>());
  }

  public String readJourneyString() {
    return commonRedisDao.readString(JOURNEY);
  }

  public void writeJourneyString(String journeyString) {
    commonRedisDao.writeString(JOURNEY, journeyString);
  }

  public void saveOgelRegistrationServiceTransactionExists(boolean transactionCreated) {
    writeBoolean(OGEL_REGISTRATION_SERVICE_TRANSACTION_EXISTS, transactionCreated);
  }

  public Optional<Boolean> getOgelRegistrationServiceTransactionExists() {
    return readBoolean(OGEL_REGISTRATION_SERVICE_TRANSACTION_EXISTS);
  }

  public void refreshTTL() {
    commonRedisDao.refreshTTL();
  }

}
