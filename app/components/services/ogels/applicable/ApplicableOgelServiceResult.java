package components.services.ogels.applicable;

import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicableOgelServiceResult {

  private static final String HISTORIC_OGEL_NAME = "historic military goods";

  public final List<ApplicableOgelView> results;

  public ApplicableOgelServiceResult(ApplicableOgelView ogel, boolean showHistoricOgel) {
    this.results = Stream.of(ogel)
    .filter(result -> showHistoricOgel || !StringUtils.containsIgnoreCase(result.getName(), HISTORIC_OGEL_NAME))
    .collect(Collectors.toList());
  }

  public Optional<ApplicableOgelView> findResultById(String ogelId) {
    if (StringUtils.isBlank(ogelId) || this.results == null || this.results.isEmpty()) {
      return Optional.empty();
    }
    return this.results.stream()
        .filter(result -> StringUtils.equals(result.getId(), ogelId))
        .findFirst();
  }

}
