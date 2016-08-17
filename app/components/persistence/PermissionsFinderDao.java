package components.persistence;

import com.google.inject.Inject;
import components.common.persistence.CommonRedisDao;
import components.common.persistence.RedisKeyConfig;
import components.common.transaction.TransactionManager;
import controllers.ogel.OgelQuestionsController;
import play.libs.Json;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PermissionsFinderDao extends CommonRedisDao {

  private static final String PHYSICAL_GOOD_CONTROL_CODE = "physicalGoodControlCode";

  private static final String SOURCE_COUNTRY = "sourceCountry";

  private static final String DESTINATION_COUNTRY_LIST = "destinationCountryList";

  private static final String OGEL_QUESTION_MAP = "ogelQuestionsMap";

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
    return Arrays.asList(Json.fromJson(Json.parse(readString(DESTINATION_COUNTRY_LIST)), String[].class));
  }

  public void saveOgelQuestionMap(Map<String, String> ogelQuestions) {
    writeString(OGEL_QUESTION_MAP, Json.stringify(Json.toJson(ogelQuestions)));
  }

  public Map<String, String> getOgelQuestionMap() {
    // TODO, see if this bind works
    OgelQuestionsController.OgelQuestionsForm form = Json.fromJson(Json.parse(readString(OGEL_QUESTION_MAP)), OgelQuestionsController.OgelQuestionsForm.class);
    return null;
  }

}
