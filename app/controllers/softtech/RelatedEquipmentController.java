package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.helpers.SoftTechJourneyHelper;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.softtech.relatedEquipment;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class RelatedEquipmentController {
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final SoftTechJourneyHelper softTechJourneyHelper;

  @Inject
  public RelatedEquipmentController(JourneyManager journeyManager,
                                    FormFactory formFactory,
                                    PermissionsFinderDao permissionsFinderDao,
                                    SoftTechJourneyHelper softTechJourneyHelper) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.softTechJourneyHelper = softTechJourneyHelper;
  }

  public Result renderForm() {
    RelatedEquipmentForm templateForm = new RelatedEquipmentForm();
    Optional<Boolean> relatedToEquipmentOrMaterialsOptional = permissionsFinderDao.getRelatedToEquipmentOrMaterials();
    templateForm.relatedToEquipmentOrMaterials = relatedToEquipmentOrMaterialsOptional.isPresent()
        ? relatedToEquipmentOrMaterialsOptional.get().toString()
        : "";
    return ok(relatedEquipment.render(formFactory.form(RelatedEquipmentForm.class).fill(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<RelatedEquipmentForm> form = formFactory.form(RelatedEquipmentForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(relatedEquipment.render(form)));
    }

    String relatedToEquipmentOrMaterials = form.get().relatedToEquipmentOrMaterials;

    if ("true".equals(relatedToEquipmentOrMaterials)) {
      permissionsFinderDao.saveRelatedToEquipmentOrMaterials(true);
      return journeyManager.performTransition(StandardEvents.YES);
    }
    else if ("false".equals(relatedToEquipmentOrMaterials)) {
      permissionsFinderDao.saveRelatedToEquipmentOrMaterials(false);
      return softTechJourneyHelper.performCatchallSoftwareControlsTransition();
    }
    else {
      throw new FormStateException(String.format("Unknown value for relatedToEquipmentOrMaterials: \"%s\"",
          relatedToEquipmentOrMaterials));
    }
  }

  public static class RelatedEquipmentForm {

    public String relatedToEquipmentOrMaterials;

  }
}
