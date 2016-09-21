package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import model.ExportCategory;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.categories.medicinesDrugs;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class MedicinesDrugsController {

  private final JourneyManager jm;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public MedicinesDrugsController(JourneyManager jm, FormFactory formFactory, PermissionsFinderDao permissionsFinderDao) {
    this.jm = jm;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    MedicinesDrugsForm templateForm = new MedicinesDrugsForm();
    Optional<Boolean> isUsedForExecutionTorture = permissionsFinderDao.getIsUsedForExecutionTorture();
    if (isUsedForExecutionTorture.isPresent()) {
      templateForm.isUsedForExecutionTorture= isUsedForExecutionTorture.get().toString();
    }
    return ok(medicinesDrugs.render(formFactory.form(MedicinesDrugsForm.class).fill(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<MedicinesDrugsForm> form = formFactory.form(MedicinesDrugsForm.class).bindFromRequest();
    if(form.hasErrors()){
      return completedFuture(ok(medicinesDrugs.render(form)));
    }

    boolean isUsedForExecutionTorture = Boolean.parseBoolean(form.get().isUsedForExecutionTorture);
    permissionsFinderDao.saveIsUsedForExecutionTorture(isUsedForExecutionTorture);

    if (isUsedForExecutionTorture) {
      permissionsFinderDao.saveExportCategory(ExportCategory.TORTURE_RESTRAINT);
      return jm.performTransition(Events.IS_USED_FOR_EXECUTION_TORTURE, true);
    }
    else {
      permissionsFinderDao.saveExportCategory(ExportCategory.MEDICINES_DRUGS);
      return jm.performTransition(Events.IS_USED_FOR_EXECUTION_TORTURE, false);
    }
  }

  public static class MedicinesDrugsForm {

    @Required(message = "You must pick an option")
    public String isUsedForExecutionTorture;

  }

}
