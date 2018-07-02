package controllers.modal;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import exceptions.UnknownParameterException;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

public class ModalContentController extends Controller {

  private static final Map<String, Html> MODAL_CONTENT_ID_MAP = ImmutableMap.<String, Html>builder()
      .put("control-entry-not-included", getStaticModalContent("controlEntryNotIncluded.html"))
      .put("legislation-not-included", getStaticModalContent("legislationNotIncluded.html"))
      .build();

  private final views.html.modal.modalContentView modalContentView;

  @Inject
  public ModalContentController(views.html.modal.modalContentView modalContentView) {
    this.modalContentView = modalContentView;
  }

  public Result renderModalContent(String modalContentId) {
    Html contentHtml = getModalContentNotNull(modalContentId);
    return ok(contentHtml);
  }

  public Result renderModalContentView(String modalContentId) {
    Html contentHtml = getModalContentNotNull(modalContentId);
    return ok(modalContentView.render(contentHtml));
  }

  private static Html getStaticModalContent(String staticModalContentFilename) {
    String name = "static/html/modal/" + staticModalContentFilename;
    try {
      URL resource = ModalContentController.class.getClassLoader().getResource(name);
      if (resource == null) {
        throw new RuntimeException("Not a file: " + name);
      } else {
        return new Html(Resources.toString(resource, Charsets.UTF_8));
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Failed to read " + name, ioe);
    }
  }

  private Html getModalContentNotNull(String modalContentId) {
    return Optional.ofNullable(MODAL_CONTENT_ID_MAP.get(modalContentId)).orElseThrow(() ->
        new UnknownParameterException("Unknown modalContentId " + modalContentId));
  }

}
