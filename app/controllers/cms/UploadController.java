package controllers.cms;

import actions.BasicAuthAction;
import com.google.inject.Inject;
import components.cms.loader.Loader;
import components.cms.parser.Parser;
import components.cms.parser.model.NavigationLevel;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UploadController extends Controller {
  private final Parser parser;
  private final Loader loader;

  @Inject
  public UploadController(Parser parser, Loader loader) {
    this.parser = parser;
    this.loader = loader;
  }

  @With(BasicAuthAction.class)
  public Result spreadsheetUpload() throws IOException {
    File file = request().body().asRaw().asFile();
    List<NavigationLevel> navigationLevels = parser.parse(file);
    loader.load(navigationLevels);
    return ok("File uploaded");
  }
}
