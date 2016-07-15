package controllers;

import com.google.inject.Inject;
import controllers.services.controlcode.lookup.LookupServiceResult;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.controlCode;

import java.util.List;

public class ControlCodeController extends Controller {

  private final FormFactory formFactory;

  @Inject
  public ControlCodeController (FormFactory formFactory) {
    this.formFactory = formFactory;
  }

  public Result renderForm(List<LookupServiceResult> lookupServiceResultList){
    return ok(controlCode.render(formFactory.form(ControlCodeForm.class), lookupServiceResultList));
  }

  public Result handleSubmit(){
    return ok("Not yet implemented");
  }

  public static class ControlCodeForm {

  }

}