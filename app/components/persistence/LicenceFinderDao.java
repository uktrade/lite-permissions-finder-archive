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

  public static final String CONTROL_CODE = "controlCode";
  public static final String SOURCE_COUNTRY = "sourceCountry";
  public static final String OGEL_ID = "ogelId";
  public static final String APPLICATION_CODE = "applicationCode";
  public static final String TRADE_TYPE = "tradeType";
  public static final String OGEL_QUESTIONS = "ogelQuestions";

  public static final String DESTINATION_COUNTRY = "destinationCountry";
  public static final String MULTIPLE_COUNTRIES = "multipleCountries";
  public static final String ROUTE_COUNTRIES = "routeCountries";

  private final CommonRedisDao dao;

  @Inject
  public LicenceFinderDao(@Named("permissionsFinderDaoHashCommon") CommonRedisDao dao) {
    this.dao = dao;
  }

  public void saveControlCode(String controlCode) {
    dao.writeString(CONTROL_CODE, controlCode);
  }

  public String getControlCode() {
    return dao.readString(CONTROL_CODE);
  }

  public void saveSourceCountry(String country) {
    dao.writeString(SOURCE_COUNTRY, country);
  }

  public String getSourceCountry() {
    return dao.readString(SOURCE_COUNTRY);
  }

  public void saveOgelId(String ogelId) {
    dao.writeString(OGEL_ID, ogelId);
  }

  public String getOgelId() {
    return dao.readString(OGEL_ID);
  }

  public void saveApplicationCode(String code) {
    dao.writeString(APPLICATION_CODE, code);
  }

  public String getApplicationCode() {
    return dao.readString(APPLICATION_CODE);
  }

  public void saveTradeType(TradeType tradeType) {
    dao.writeString(TRADE_TYPE, tradeType.toString());
  }

  public Optional<TradeType> getTradeType() {
    String tradeType = dao.readString(TRADE_TYPE);
    return StringUtils.isBlank(tradeType) ? Optional.empty() : Optional.of(TradeType.valueOf(tradeType));
  }

  public void writeBoolean(String fieldName, boolean value) {
    dao.writeString(fieldName, Boolean.toString(value));
  }

  public Optional<Boolean> readBoolean(String fieldName) {
    String value = dao.readString(fieldName);
    if (value == null || value.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(value.equalsIgnoreCase("true"));
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

  public void saveDestinationCountry(String country) {
    dao.writeString(DESTINATION_COUNTRY, country);
  }

  public String getDestinationCountry() {
    return dao.readString(DESTINATION_COUNTRY);
  }

  public void saveRouteCountries(List<String> countries) {
    dao.writeObject(ROUTE_COUNTRIES, countries);
  }

  public List<String> getRouteCountries() {
    return dao.readObject(ROUTE_COUNTRIES, new TypeReference<List<String>>() {
    }).orElse(new ArrayList<>());
  }
}
