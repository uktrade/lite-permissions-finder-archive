package controllers.cms;

import actions.BasicAuthAction;
import com.google.inject.Inject;
import components.cms.loader.Loader;
import components.cms.parser.Parser;
import components.cms.parser.ParserResult;
import lombok.AllArgsConstructor;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import triage.cache.CachePopulationService;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class UploadController extends Controller {
  private final Parser parser;
  private final Loader loader;
  private final CachePopulationService cachePopulationService;

  @With(BasicAuthAction.class)
  public Result spreadsheetUpload() throws IOException, NoSuchAlgorithmException {
    Http.MultipartFormData.FilePart filePart = request().body().asMultipartFormData().getFile("spreadsheet");
    File file = (File)filePart.getFile();
    ParserResult parserResult = parser.parse(file, filePart.getFilename());

    loader.load(parserResult);
    cachePopulationService.populateCache();
    return ok("File uploaded");
  }
}
