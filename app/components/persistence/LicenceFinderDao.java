package components.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.persistence.CommonRedisDao;
import controllers.licencefinder.LicenceFinderController;
import models.TradeType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LicenceFinderDao {

  private static final String CONTROL_CODE = "controlCode";
  private static final String SOURCE_COUNTRY = "sourceCountry";
  private static final String OGEL_ID = "ogelId";
  private static final String CUSTOMER_ID = "customerId";
  private static final String SITE_ID = "siteId";
  private static final String APPLICATION_CODE = "applicationCode";
  private static final String TRADE_TYPE = "tradeType";
  private static final String OGEL_QUESTIONS = "ogelQuestions";
  private static final String DESTINATION_COUNTRY = "destinationCountry";
  private static final String MULTIPLE_COUNTRIES = "multipleCountries";
  private static final String ROUTE_COUNTRIES = "routeCountries";

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

  public void saveControlCode(String arg) {
    dao.writeString(CONTROL_CODE, arg);
  }

  public String getControlCode() {
    return dao.readString(CONTROL_CODE);
  }

  public void saveSourceCountry(String arg) {
    dao.writeString(SOURCE_COUNTRY, arg);
  }

  public String getSourceCountry() {
    return dao.readString(SOURCE_COUNTRY);
  }

  public void saveOgelId(String arg) {
    dao.writeString(OGEL_ID, arg);
  }

  public String getOgelId() {
    return dao.readString(OGEL_ID);
  }

  public void saveApplicationCode(String arg) {
    dao.writeString(APPLICATION_CODE, arg);
  }

  public String getApplicationCode() {
    return dao.readString(APPLICATION_CODE);
  }

  public void saveDestinationCountry(String arg) {
    dao.writeString(DESTINATION_COUNTRY, arg);
  }

  public String getDestinationCountry() {
    return dao.readString(DESTINATION_COUNTRY);
  }

  public void saveTradeType(TradeType tradeType) {
    dao.writeString(TRADE_TYPE, tradeType.toString());
  }

  public Optional<TradeType> getTradeType() {
    String tradeType = dao.readString(TRADE_TYPE);
    return StringUtils.isBlank(tradeType) ? Optional.empty() : Optional.of(TradeType.valueOf(tradeType));
  }

  public void saveQuestionsForm(LicenceFinderController.QuestionsForm form) {
    dao.writeObject(OGEL_QUESTIONS, form);
  }

  public Optional<LicenceFinderController.QuestionsForm> getQuestionsForm() {
    return dao.readObject(OGEL_QUESTIONS, LicenceFinderController.QuestionsForm.class);
  }

  public void saveMultipleCountries(boolean countries) {
    writeBoolean(MULTIPLE_COUNTRIES, countries);
  }

  public Optional<Boolean> getMultipleCountries() {
    return readBoolean(MULTIPLE_COUNTRIES);
  }

  public void saveRouteCountries(List<String> countries) {
    dao.writeObject(ROUTE_COUNTRIES, countries);
  }

  public List<String> getRouteCountries() {
    return dao.readObject(ROUTE_COUNTRIES, new TypeReference<List<String>>() {
    }).orElse(new ArrayList<>());
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
