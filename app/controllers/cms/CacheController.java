package controllers.cms;

import actions.BasicAuthAction;
import com.google.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import triage.cache.CachePopulationService;

public class CacheController extends Controller {

  private final CachePopulationService cachePopulationService;

  @Inject
  public CacheController(CachePopulationService cachePopulationService) {
    this.cachePopulationService = cachePopulationService;
  }

  @With(BasicAuthAction.class)
  public Result populateCaches() {
    String result = cachePopulationService.populateCache();
    return ok(result);
  }
}
