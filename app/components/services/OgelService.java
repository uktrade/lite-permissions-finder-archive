package components.services;

import uk.gov.bis.lite.ogel.api.view.OgelFullView;

import java.util.concurrent.CompletionStage;

public interface OgelService {

  CompletionStage<OgelFullView> getById(String ogelId);

}
