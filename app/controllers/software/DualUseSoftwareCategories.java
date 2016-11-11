package controllers.software;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import models.software.SoftwareCategory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.software.dualUseSoftwareCategories;

import java.util.concurrent.CompletionStage;

public class DualUseSoftwareCategories {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public DualUseSoftwareCategories(FormFactory formFactory, PermissionsFinderDao permissionsFinderDao, JourneyManager journeyManager) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.journeyManager = journeyManager;
  }

  public Result renderForm() {
    return ok(dualUseSoftwareCategories.render(formFactory.form(DualUseSoftwareCategoriesForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<DualUseSoftwareCategoriesForm> form = formFactory.form(DualUseSoftwareCategoriesForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(dualUseSoftwareCategories.render(form)));
    }
    String dualUseSoftwareCategoryText = form.get().dualUseSoftwareCategory;
    SoftwareCategory softwareCategory = SoftwareCategory.valueOf(dualUseSoftwareCategoryText);

    // MILITARY is a member of SoftwareCategory but is not dual use
    if (SoftwareCategory.isDualUseSoftwareCategory(softwareCategory)) {
      permissionsFinderDao.saveDualUseSoftwareCategory(softwareCategory);
      return journeyManager.performTransition(Events.DUAL_USE_SOFTWARE_CATEGORY_SELECTED);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of SoftwareCategory enum: \"%s\""
          , softwareCategory.toString()));
    }
  }


  public static class DualUseSoftwareCategoriesForm {

    public String dualUseSoftwareCategory;

  }
}
