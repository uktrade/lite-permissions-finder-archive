package components.client;

import uk.gov.bis.lite.ogel.api.view.OgelFullView;

import java.util.concurrent.CompletionStage;

public interface OgelServiceClient {

  CompletionStage<OgelFullView> getById(String ogelId);

  CompletionStage<Boolean> ping();

}
