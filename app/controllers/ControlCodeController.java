package controllers;

import com.google.inject.Inject;
import controllers.services.controlcode.lookup.LookupServiceResult;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import scala.Option;
import views.html.controlCode;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControlCodeController extends Controller {

  private final FormFactory formFactory;

  @Inject
  public ControlCodeController (FormFactory formFactory) {
    this.formFactory = formFactory;
  }

  public Result renderForm(List<LookupServiceResult> lookupServiceResults){
    LookupServiceResult primaryCode = lookupServiceResults.get(0);
    Option<LookupServiceResult> greatestAncestorCode = lookupServiceResults.size() > 1 ? Option.apply(lookupServiceResults.get(1)) : Option.empty();
    List<LookupServiceResult> otherAncestorCodes = IntStream.range(0, lookupServiceResults.size())
        .filter(i -> i > 1 && i < lookupServiceResults.size())
        .mapToObj(lookupServiceResults::get)
        .collect(Collectors.toList());
    return ok(controlCode.render(formFactory.form(ControlCodeForm.class), primaryCode, greatestAncestorCode, otherAncestorCodes));
  }

  public Result handleSubmit(){
    return ok("Not yet implemented");
  }

  public static class ControlCodeForm {

    String couldDescribeItems;

  }

}