package components.services.ogels.applicable;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApplicableOgelServiceResult {

  private static final String HISTORIC_OGEL_NAME = "historic military goods";

  public final List<Result> results;

  public ApplicableOgelServiceResult(JsonNode responseJson, boolean showHistoricOgel) {
    this.results = Arrays.asList(Json.fromJson(responseJson, Result[].class)).stream()
    .filter(result -> showHistoricOgel || !StringUtils.containsIgnoreCase(result.name, HISTORIC_OGEL_NAME))
    .collect(Collectors.toList());
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
