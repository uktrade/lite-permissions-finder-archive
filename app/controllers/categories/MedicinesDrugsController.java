package controllers.categories;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import controllers.StaticContentController;
import model.ExportCategory;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.categories.medicinesDrugs;

public class MedicinesDrugsController {

  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;
  private final StaticContentController staticContentController;
  private final TortureRestraintController tortureRestraintController;

  @Inject
  public MedicinesDrugsController(FormFactory formFactory, PermissionsFinderDao dao, StaticContentController staticContentController, TortureRestraintController tortureRestraintController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.staticContentController = staticContentController;
    this.tortureRestraintController = tortureRestraintController;
  }

  public Result renderForm() {
    return ok(medicinesDrugs.render(formFactory.form(MedicinesDrugsForm.class)));
  }

  public Result handleSubmit() {
    Form<MedicinesDrugsForm> form = formFactory.form(MedicinesDrugsForm.class).bindFromRequest();
    if(form.hasErrors()){
      return ok(medicinesDrugs.render(form));
    }
    String isUsedForExecutionTorture = form.get().isUsedForExecutionTorture;
    if ("true".equals(isUsedForExecutionTorture)) {
      dao.saveExportCategory(ExportCategory.TORTURE_RESTRAINT);
      return tortureRestraintController.renderForm();
    }
    if ("false".equals(isUsedForExecutionTorture)) {
      dao.saveExportCategory(ExportCategory.MEDICINES_DRUGS);
      return staticContentController.renderStaticHtml(StaticContentController.StaticHtml.CATEGORY_MEDICINES_DRUGS);
    }
    return badRequest("Unknown value for isUsedForExecutionTorture: \"" + isUsedForExecutionTorture + "\"");
  }

  public static class MedicinesDrugsForm {

    @Required(message = "You must pick an option")
    public String isUsedForExecutionTorture;

  }

}
