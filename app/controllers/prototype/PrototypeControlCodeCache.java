package controllers.prototype;

import com.google.inject.Inject;
import components.services.controlcode.prototype.PrototypeControlCodeServiceClient;
import components.services.controlcode.related.RelatedControlsServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class PrototypeControlCodeCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(PrototypeControlCodeCache.class);
  private volatile Map<String, ControlCodeFullView> cache = new HashMap<>();
  private PrototypeControlCodeServiceClient controlCodeServiceClient;

  @Inject
  public PrototypeControlCodeCache(PrototypeControlCodeServiceClient controlCodeServiceClient) {
    this.controlCodeServiceClient = controlCodeServiceClient;
  }

  public void load() throws ExecutionException, InterruptedException {
    LOGGER.info("Loading cache.........");
    List<ControlCodeFullView> controlCodes = getControlCodes();
    Map<String, ControlCodeFullView> cacheMap = controlCodes.stream().collect(Collectors.toMap(e -> e.getControlCode(), e -> e));
    cache = Collections.unmodifiableMap(cacheMap);

  }

  public Map<String, ControlCodeFullView> getCache() {
    LOGGER.info("Get cache.........");
    return cache;
  }

  private List<ControlCodeFullView> getControlCodes() throws ExecutionException, InterruptedException {
    CompletionStage<RelatedControlsServiceResult> result = controlCodeServiceClient.get();
    return result.toCompletableFuture().get().getControlCodes();
  }


}
