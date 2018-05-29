package components.client;

import uk.gov.bis.lite.ogel.api.view.OgelFullView;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface OgelService {

  CompletionStage<Optional<OgelFullView>> getOgel(String ogelId);

  CompletionStage<OgelFullView> get(String ogelId);

}
