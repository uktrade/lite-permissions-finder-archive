package controllers.cms;

import actions.BasicAuthAction;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import java.io.File;

public class UploadController extends Controller {
  @With(BasicAuthAction.class)
  public Result spreadsheetUpload() {
    File file = request().body().asRaw().asFile();
    return ok("File uploaded");
  }
}
