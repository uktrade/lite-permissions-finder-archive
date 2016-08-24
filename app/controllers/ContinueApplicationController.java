package controllers;

import static play.mvc.Results.ok;
import static play.mvc.Results.badRequest;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.continueApplication;

public class ContinueApplicationController {

  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;
  private final StartApplicationController startApplicationController;
  private final TradeTypeController tradeTypeController;

  @Inject
  public ContinueApplicationController(FormFactory formFactory,
                                       PermissionsFinderDao dao,
                                       StartApplicationController startApplicationController,
                                       TradeTypeController tradeTypeController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.startApplicationController = startApplicationController;
    this.tradeTypeController = tradeTypeController;
  }

  public Result renderForm() {
    return ok(continueApplication.render(formFactory.form(ContinueApplicationForm.class)));
  }

  public Result handleSubmit() {
    Form<ContinueApplicationForm> form = formFactory.form(ContinueApplicationForm.class).bindFromRequest();
    if ("true".equals(form.field("startApplication").value())) {
      return startApplicationController.renderForm();
    }
    if (form.hasErrors()) {
      return ok(continueApplication.render(form));
    }
    String applicationNumber = form.get().applicationNumber;
    String memorableWord = form.get().memorableWord;
    if (applicationNumber != null && !applicationNumber.isEmpty() && memorableWord != null && !memorableWord.isEmpty()) {
      if (applicationNumber.equals(dao.getApplicationCode()) && memorableWord.equals(dao.getMemorableWord())) {
        return tradeTypeController.renderForm();
      }
      // TODO Add in additional screen catering for this condition IELS-606
      return ok("COULD NOT FIND APPLICATION WITH SUPPLIED DATA");
    }
    return badRequest("Unhandled form state");
  }

  public static class ContinueApplicationForm {

    @Required(message = "You must enter your application number")
    public String applicationNumber;

    @Required(message = "You must enter your memorable word")
    public String memorableWord;

  }

}

