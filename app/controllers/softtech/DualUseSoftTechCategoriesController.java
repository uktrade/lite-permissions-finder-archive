package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.SoftTechJourneyHelper;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.SoftwareCategory;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.softtech.dualUseSoftTechCategories;

import java.util.concurrent.CompletionStage;

public class DualUseSoftTechCategoriesController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final SoftTechJourneyHelper softTechJourneyHelper;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public DualUseSoftTechCategoriesController(FormFactory formFactory, PermissionsFinderDao permissionsFinderDao,
                                             JourneyManager journeyManager, SoftTechJourneyHelper softTechJourneyHelper, HttpExecutionContext httpExecutionContext) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.journeyManager = journeyManager;
    this.softTechJourneyHelper = softTechJourneyHelper;
    this.httpExecutionContext = httpExecutionContext;
  }

  public Result renderForm() {
    return ok(dualUseSoftTechCategories.render(formFactory.form(DualUseSoftwareCategoriesForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<DualUseSoftwareCategoriesForm> form = formFactory.form(DualUseSoftwareCategoriesForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(dualUseSoftTechCategories.render(form)));
    }
    String dualUseSoftwareCategoryText = form.get().dualUseSoftwareCategory;
    String action = form.get().action;

    if (StringUtils.isNotEmpty(action)) {
      if ("noneOfTheAbove".equals(action)) {
        return journeyManager.performTransition(Events.NONE_MATCHED);
      }
      else {
        throw new FormStateException(String.format("Unknown value for action: \"%s\"", action));
      }
    }
    // MILITARY is a member of SoftwareCategory but is not dual use
    else if (StringUtils.isNotEmpty(dualUseSoftwareCategoryText)) {
      SoftwareCategory softwareCategory = SoftwareCategory.valueOf(dualUseSoftwareCategoryText);
      if (softwareCategory.isDualUseSoftwareCategory()) {
        permissionsFinderDao.saveSoftwareCategory(softwareCategory);
        return softTechJourneyHelper.checkSoftwareControls(softwareCategory, true) // Save to DAO
            .thenComposeAsync(this::dualUseSoftwareCategorySelected, httpExecutionContext.current());
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of SoftwareCategory enum: \"%s\""
            , softwareCategory.toString()));
      }
    }
    else {
      throw new FormStateException("Unhandled form state");
    }
  }

  private CompletionStage<Result> dualUseSoftwareCategorySelected(ApplicableSoftTechControls applicableSoftTechControls) {
    return journeyManager.performTransition(Events.DUAL_USE_SOFTWARE_CATEGORY_SELECTED, applicableSoftTechControls);
  }

  public static class DualUseSoftwareCategoriesForm {

    public String dualUseSoftwareCategory;

    public String action;

  }
}
