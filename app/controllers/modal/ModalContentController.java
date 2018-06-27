package controllers.modal;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class ModalContentController extends Controller {
  private final views.html.modal.modalContentView modalContentView;
  private static final Map<String, String> MODAL_CONTENT_ID_MAP = ImmutableMap.<String, String>builder()
      .put("control-entry-not-included", "controlEntryNotIncluded.html")
      .put("legislation-not-included", "legislationNotIncluded.html")
      .build();

  @Inject
  public ModalContentController(views.html.modal.modalContentView modalContentView) {
    this.modalContentView = modalContentView;
  }

  public Result renderModalContent(String modalContentId) {
    Html contentHtml = resolveModalContentIdToStaticHtml(modalContentId);
    return ok(contentHtml);
  }

  public Result renderModalContentView(String modalContentId) {
    Html contentHtml = resolveModalContentIdToStaticHtml(modalContentId);
    return ok(modalContentView.render(contentHtml));
  }

  private Html resolveModalContentIdToStaticHtml(String modalContentId) {
    return MODAL_CONTENT_ID_MAP.keySet()
        .stream()
        .filter(id -> id.equals(modalContentId))
        .map(MODAL_CONTENT_ID_MAP::get)
        .map(this::getStaticModalContent)
        .findAny()
        .orElseThrow(() -> new RuntimeException(String.format("Unknown modalContentId %s ", modalContentId)));
  }

  private Html getStaticModalContent(String staticModalContentFilename) {
    try {
      URL resource = getClass().getClassLoader().getResource("static/html/modal/" + staticModalContentFilename);
      if (resource == null) {
        throw new RuntimeException("Not a file: static/html/modal/" + staticModalContentFilename);
      }

      return new Html(Resources.toString(resource, Charsets.UTF_8));

    } catch (IOException e) {
      throw new RuntimeException("Failed to read", e);
    }
  }
}
