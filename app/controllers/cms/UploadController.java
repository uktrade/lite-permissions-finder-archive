package controllers.cms;

import actions.BasicAuthAction;
import com.google.inject.Inject;
import components.cms.loader.Loader;
import components.cms.parser.Parser;
import components.cms.parser.ParserResult;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import triage.cache.CachePopulationService;

import java.io.File;
import java.io.IOException;

public class UploadController extends Controller {
  private final Parser parser;
  private final Loader loader;
  private final CachePopulationService cachePopulationService;

  @Inject
  public UploadController(Parser parser, Loader loader, CachePopulationService cachePopulationService) {
    this.parser = parser;
    this.loader = loader;
    this.cachePopulationService = cachePopulationService;
  }

  @With(BasicAuthAction.class)
  public Result spreadsheetUpload() throws IOException {
    File file = request().body().asRaw().asFile();
    ParserResult parserResult = parser.parse(file);
    loader.load(parserResult);
    cachePopulationService.populateCache();
    return ok("File uploaded");
  }
}
