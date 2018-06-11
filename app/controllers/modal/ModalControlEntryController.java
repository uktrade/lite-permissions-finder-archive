package controllers.modal;

import com.google.inject.Inject;
import components.services.BreadcrumbViewService;
import models.view.BreadcrumbItemView;
import play.mvc.Controller;
import play.mvc.Result;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.text.HtmlRenderOption;
import views.html.modal.modalControlEntry;

import java.util.List;

public class ModalControlEntryController extends Controller {
  private final JourneyConfigService journeyConfigService;
  private final BreadcrumbViewService breadcrumbViewService;
  private final views.html.modal.modalControlEntryView modalControlEntryView;

  @Inject
  public ModalControlEntryController(JourneyConfigService journeyConfigService,
                                     BreadcrumbViewService breadcrumbViewService,
                                     views.html.modal.modalControlEntryView modalControlEntryView) {
    this.journeyConfigService = journeyConfigService;
    this.breadcrumbViewService = breadcrumbViewService;
    this.modalControlEntryView = modalControlEntryView;
  }

  public Result renderControlEntryModal(String controlEntryId) {
    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(controlEntryId);
    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(null, controlEntryConfig);
    return ok(modalControlEntry.render(controlEntryConfig.getControlCode(), breadcrumbItemViews));
  }

  public Result renderControlEntryView(String controlEntryId) {
    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(controlEntryId);
    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(null, controlEntryConfig, HtmlRenderOption.OMIT_LINK_TARGET_ATTR);
    return ok(modalControlEntryView.render(controlEntryConfig.getControlCode(), breadcrumbItemViews));
  }
}
