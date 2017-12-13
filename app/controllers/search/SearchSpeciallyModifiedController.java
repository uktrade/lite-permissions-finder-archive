package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import models.search.SpeciallyModifiedDisplay;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.search.searchSpeciallyModified;

import java.util.concurrent.CompletionStage;

public class SearchSpeciallyModifiedController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public SearchSpeciallyModifiedController(JourneyManager journeyManager,
                                           FormFactory formFactory,
                                           PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public CompletionStage<Result> renderForm(Boolean isComponent) {
    SpeciallyModifiedSearchForm templateForm = new SpeciallyModifiedSearchForm();
    return completedFuture(ok(searchSpeciallyModified.render(formFactory.form(SpeciallyModifiedSearchForm.class).fill(templateForm),
        new SpeciallyModifiedDisplay(isComponent))));
  }

  public CompletionStage<Result> handleSubmit(String isModified) {
    return null;
  }

  public static class SpeciallyModifiedSearchForm {

    @Required(message = "Select whether this item is specifically designed or modified")
    public Boolean isSpecial;

  }

}