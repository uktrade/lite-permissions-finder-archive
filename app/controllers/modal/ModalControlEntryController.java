package controllers.modal;

import com.google.inject.Inject;
import components.services.BreadcrumbViewService;
import models.view.BreadcrumbItemView;
import play.mvc.Controller;
import play.mvc.Result;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import views.html.modal.modalControlEntry;

import java.util.List;

public class ModalControlEntryController extends Controller {
  private final JourneyConfigService journeyConfigService;
  private final BreadcrumbViewService breadcrumbViewService;

  @Inject
  public ModalControlEntryController(JourneyConfigService journeyConfigService,
                                     BreadcrumbViewService breadcrumbViewService) {
    this.journeyConfigService = journeyConfigService;
    this.breadcrumbViewService = breadcrumbViewService;
  }

  public Result renderControlEntry(String controlEntryId) {
    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(controlEntryId);
    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(null, controlEntryConfig);
    return ok(modalControlEntry.render(controlEntryConfig.getControlCode(), breadcrumbItemViews));
  }
}
