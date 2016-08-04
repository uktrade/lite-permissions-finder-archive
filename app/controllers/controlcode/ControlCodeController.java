package controllers.controlcode;

import com.google.inject.Inject;
import controllers.services.controlcode.frontend.Ancestor;
import controllers.services.controlcode.frontend.FrontendServiceResult;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import scala.Option;
import views.html.controlcode.controlCode;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControlCodeController extends Controller {

  private final FormFactory formFactory;

  private final AdditionalSpecificationsController additionalSpecificationsController;

  @Inject
  public ControlCodeController (FormFactory formFactory, AdditionalSpecificationsController additionalSpecificationsController) {
    this.formFactory = formFactory;
    this.additionalSpecificationsController = additionalSpecificationsController;
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
    return additionalSpecificationsController.renderForm();
  }

  public static class ControlCodeForm {

    public String couldDescribeItems;

  }

}