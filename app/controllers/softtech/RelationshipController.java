package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.softtech.relationship;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class RelationshipController {
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public RelationshipController(JourneyManager journeyManager, FormFactory formFactory,
                                PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    RelationshipForm templateForm = new RelationshipForm();
    Optional<Boolean> isCoveredByRelationship = permissionsFinderDao.getSoftwareIsCoveredByTechnologyRelationship();
    templateForm.isCoveredByRelationship = isCoveredByRelationship.isPresent() ? isCoveredByRelationship.get().toString() : "";
    return ok(relationship.render(formFactory.form(RelationshipForm.class).fill(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<RelationshipForm> form = formFactory.form(RelationshipForm.class).bindFromRequest();
    if (form.hasErrors()) {
      completedFuture(ok(relationship.render(form)));
    }
    String action = form.get().isCoveredByRelationship;
    if ("true".equals(action)) {
      return journeyManager.performTransition(StandardEvents.YES);
    }
    else if ("false".equals(action)) {
      return journeyManager.performTransition(StandardEvents.NO);
    }
    else {
      throw new FormStateException(String.format("Unknown value for action: \"%s\"", action));
    }
  }

  public static class RelationshipForm {
    @Required(message = "This is required")
    public String isCoveredByRelationship;
  }

}