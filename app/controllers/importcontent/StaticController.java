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

  public static final String EP1_KEY = "importEp1";
  public static final String EP1_TITLE = "Ep1title";
  public static final String EP2_KEY = "importEp2";
  public static final String EP2_TITLE = "Ep2title";
  public static final String EP3_KEY = "importEp3";
  public static final String EP3_TITLE = "Importing from Crimea";
  public static final String EP4_KEY = "importEp4";
  public static final String EP4_TITLE = "Ep4title";
  public static final String EP5_KEY = "importEp5";
  public static final String EP5_TITLE = "Ep5title";
  public static final String EP6_KEY = "importEp6";
  public static final String EP6_TITLE = "Ep6title";
  public static final String EP7_KEY = "importEp7";
  public static final String EP7_TITLE = "Ep7title";
  public static final String EP8_KEY = "importEp8";
  public static final String EP8_TITLE = "Ep8title";
  public static final String EP9_KEY = "importEp9";
  public static final String EP9_TITLE = "Ep9title";
  public static final String EP10_KEY = "importEp10";
  public static final String EP10_TITLE = "Ep10title";
  public static final String EP11_KEY = "importEp11";
  public static final String EP11_TITLE = "Ep11title";
  public static final String EP12_KEY = "importEp12";
  public static final String EP12_TITLE = "Ep12title";
  public static final String EP13_KEY = "importEp13";
  public static final String EP13_TITLE = "Ep13title";
  public static final String EP14_KEY = "importEp14";
  public static final String EP14_TITLE = "Ep14title";
  public static final String EP15_KEY = "importEp15";
  public static final String EP15_TITLE = "Ep15title";
  public static final String EP16_KEY = "importEp16";
  public static final String EP16_TITLE = "Ep16title";
  public static final String EP17_KEY = "importEp17";
  public static final String EP17_TITLE = "Ep17title";
  public static final String EP18_KEY = "importEp18";
  public static final String EP18_TITLE = "Ep18title";
  public static final String EP19_KEY = "importEp19";
  public static final String EP19_TITLE = "Ep19title";
  public static final String EP20_KEY = "importEp20";
  public static final String EP20_TITLE = "Ep20title";
  public static final String EP21_KEY = "importEp21";
  public static final String EP21_TITLE = "Ep21title";
  public static final String EP22_KEY = "importEp22";
  public static final String EP22_TITLE = "Ep22title";
  public static final String EP23_KEY = "importEp23";
  public static final String EP23_TITLE = "Ep23title";
  public static final String EP24_KEY = "importEp24";
  public static final String EP24_TITLE = "Ep24title";
  public static final String EP25_KEY = "importEp25";
  public static final String EP25_TITLE = "Ep25title";
  public static final String EP26_KEY = "importEp26";
  public static final String EP26_TITLE = "Ep26title";
  public static final String EP27_KEY = "importEp27";
  public static final String EP27_TITLE = "Ep27title";
  public static final String EP28_KEY = "importEp28";
  public static final String EP28_TITLE = "Ep28title";
  public static final String EP29_KEY = "importEp29";
  public static final String EP29_TITLE = "Ep29title";
  public static final String EP30_KEY = "importEp30";
  public static final String EP30_TITLE = "Ep30title";
  public static final String EP31_KEY = "importEp31";
  public static final String EP31_TITLE = "Ep3title";


  public enum ImportHtml {

    IMPORT_EP1("importcontent/" + EP1_KEY + ".html", EP1_TITLE),
    IMPORT_EP2("importcontent/" + EP2_KEY + ".html", EP2_TITLE),
    IMPORT_EP3("importcontent/" + EP3_KEY + ".html", EP3_TITLE),
    IMPORT_EP4("importcontent/" + EP4_KEY + ".html", EP4_TITLE),
    IMPORT_EP5("importcontent/" + EP5_KEY + ".html", EP5_TITLE),
    IMPORT_EP6("importcontent/" + EP6_KEY + ".html", EP6_TITLE),
    IMPORT_EP7("importcontent/" + EP7_KEY + ".html", EP7_TITLE),
    IMPORT_EP8("importcontent/" + EP8_KEY + ".html", EP8_TITLE),
    IMPORT_EP9("importcontent/" + EP9_KEY + ".html", EP9_TITLE),
    IMPORT_EP10("importcontent/" + EP10_KEY + ".html", EP10_TITLE),
    IMPORT_EP11("importcontent/" + EP11_KEY + ".html", EP11_TITLE),
    IMPORT_EP12("importcontent/" + EP12_KEY + ".html", EP12_TITLE),
    IMPORT_EP13("importcontent/" + EP13_KEY + ".html", EP13_TITLE),
    IMPORT_EP14("importcontent/" + EP14_KEY + ".html", EP14_TITLE),
    IMPORT_EP15("importcontent/" + EP15_KEY + ".html", EP15_TITLE),
    IMPORT_EP16("importcontent/" + EP16_KEY + ".html", EP16_TITLE),
    IMPORT_EP17("importcontent/" + EP17_KEY + ".html", EP17_TITLE),
    IMPORT_EP18("importcontent/" + EP18_KEY + ".html", EP18_TITLE),
    IMPORT_EP19("importcontent/" + EP19_KEY + ".html", EP19_TITLE),
    IMPORT_EP20("importcontent/" + EP20_KEY + ".html", EP20_TITLE),
    IMPORT_EP21("importcontent/" + EP21_KEY + ".html", EP21_TITLE),
    IMPORT_EP22("importcontent/" + EP22_KEY + ".html", EP22_TITLE),
    IMPORT_EP23("importcontent/" + EP23_KEY + ".html", EP23_TITLE),
    IMPORT_EP24("importcontent/" + EP24_KEY + ".html", EP24_TITLE),
    IMPORT_EP25("importcontent/" + EP25_KEY + ".html", EP25_TITLE),
    IMPORT_EP26("importcontent/" + EP26_KEY + ".html", EP26_TITLE),
    IMPORT_EP27("importcontent/" + EP27_KEY + ".html", EP27_TITLE),
    IMPORT_EP28("importcontent/" + EP28_KEY + ".html", EP28_TITLE),
    IMPORT_EP29("importcontent/" + EP29_KEY + ".html", EP29_TITLE),
    IMPORT_EP30("importcontent/" + EP30_KEY + ".html", EP30_TITLE),
    IMPORT_EP31("importcontent/" + EP31_KEY + ".html", EP31_TITLE),

    NOT_IMPLEMENTED("notImplemented.html", "This section is currently under development");

    private final String filename;
    private final String title;

    ImportHtml(String filename, String title) {
      this.filename = filename;
      this.title = title;
    }

  }

  public Result render(String importHtmlName) {
    ImportHtml html = ImportHtml.valueOf(importHtmlName);
    try {
      URL resource = getClass().getClassLoader().getResource("static/html/" + html.filename);
      if (resource == null) {
        throw new RuntimeException("Not a file: " + html.filename);
      } else {
        return ok(staticContent.render(html.title, new Html(Resources.toString(resource, Charsets.UTF_8))));
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read", e);
    }
  }

}

