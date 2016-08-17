package controllers.ogel;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import components.services.ogel.OgelServiceResult;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.ogel.ogelResults;

import java.util.ArrayList;
import java.util.List;

public class OgelResultsController {

  private final FormFactory formFactory;

  private final PermissionsFinderDao dao;

  private final HttpExecutionContext ec;

  private final List<OgelServiceResult> ogels;

  @Inject
  public OgelResultsController(FormFactory formFactory, PermissionsFinderDao dao, HttpExecutionContext ec) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.ec = ec;
    OgelServiceResult ogel = new OgelServiceResult();
    ogel.id = "1";
    ogel.title = "Open general export licence (export after repair/replacement under warranty: dual-use items)";
    ogel.additionalText = "Export dual-use items that were previously imported to the UK or EU for repair or replacement under warranty.";
    ogels = new ArrayList<OgelServiceResult>();
    ogels.add(ogel);
    ogels.add(ogel);
    ogels.add(ogel);
  }

  public Result renderForm() {
    // TODO hook into ogel service
    // TODO handle no results path
    return ok(ogelResults.render(formFactory.form(OgelResultsForm.class), ogels));
  }

  public Result handleSubmit() {
    Form<OgelResultsForm> form = formFactory.form(OgelResultsForm.class).bindFromRequest();

    if (form.hasErrors()) {
      return ok(ogelResults.render(form, ogels));
    }

    return ok("Selected OGEL: " + form.get().chosenOgel);
  }

  public static class OgelResultsForm {

    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;

  }

}
