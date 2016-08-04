package controllers;

import com.google.inject.Inject;
import controllers.services.controlcode.frontend.Ancestor;
import controllers.services.controlcode.frontend.FrontendServiceResult;
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

  public Result renderForm(FrontendServiceResult frontendServiceResult) {

    Option<Ancestor> greatestAncestorCode = !frontendServiceResult.ancestors.isEmpty() ? Option.apply(frontendServiceResult.ancestors.get(frontendServiceResult.ancestors.size() -1)) : Option.empty();
    List<Ancestor> otherAncestorCodes = IntStream.range(0, frontendServiceResult.ancestors.size() - 1)
        .filter(i -> i < frontendServiceResult.ancestors.size() - 1)
        .mapToObj(i -> frontendServiceResult.ancestors.get(i))
        .collect(Collectors.toList());

    return ok(controlCode.render(formFactory.form(ControlCodeForm.class), frontendServiceResult.controlCodeData, greatestAncestorCode, otherAncestorCodes));
  }

  public Result handleSubmit() {
    return ok("Not yet implemented");
  }

  public static class ControlCodeForm {

    public String couldDescribeItems;

  }

}