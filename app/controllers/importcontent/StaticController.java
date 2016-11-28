package controllers.importcontent;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import views.html.importcontent.staticContent;

import java.io.IOException;
import java.net.URL;

public class StaticController extends Controller {

  public Result render(String key) {
    try {
      URL resource = getClass().getClassLoader().getResource("static/html/importcontent/" + key + ".html");
      if (resource == null) {
        throw new RuntimeException("Not a file: " + key);
      } else {
        return ok(staticContent.render("d", new Html(Resources.toString(resource, Charsets.UTF_8))));
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read", e);
    }
  }
}

