package components.services.ogels.applicable;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.StringUtils;
import play.libs.Json;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ApplicableOgelServiceResult {

  public final List<Result> results;

  public ApplicableOgelServiceResult(JsonNode responseJson) {
    this.results = Arrays.asList(Json.fromJson(responseJson, Result[].class));
  }

  public Optional<Result> findResultById(String ogelId) {
    if (StringUtils.isBlank(ogelId) || this.results == null || this.results.isEmpty()) {
      return Optional.empty();
    }
    return this.results.stream()
        .filter(result -> StringUtils.equals(result.id, ogelId))
        .findFirst();
  }

}
