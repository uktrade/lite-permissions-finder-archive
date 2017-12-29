package controllers.prototype;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import controllers.prototype.enums.PrototypeEquipment;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;
import views.html.prototype.prototypeControlCode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class PrototypeControlCodeController {

  private final FormFactory formFactory;
  private final PrototypeControlCodeCache cache;

  @Inject
  public PrototypeControlCodeController(FormFactory formFactory,
                                        PrototypeControlCodeCache cache) {
    this.formFactory = formFactory;
    this.cache = cache;
  }


  private Result renderWithForm(String controlCodeParam, Form<PrototypeControlCodeForm> form) throws ExecutionException, InterruptedException {

    Map<String, ControlCodeFullView> result;
    if (cache.getCache().isEmpty()) {
      cache.load();
      result = cache.getCache();
    } else {
      result = cache.getCache();
    }

    ControlCodeFullView givenControlCode = result.get(controlCodeParam);
    PrototypeTreeNode<ControlCodeFullView> root = new PrototypeTreeNode<>(givenControlCode);
    root = recursiveFunc(result, root);

    return ok(prototypeControlCode.render(form, root));
  }

  private PrototypeTreeNode<ControlCodeFullView> recursiveFunc(Map<String, ControlCodeFullView> result, PrototypeTreeNode<ControlCodeFullView> node) {
    List<ControlCodeFullView> children = result.values().stream().filter(d -> d.getParentId().equals(node.data.getId())).collect(Collectors.toList());
    if (children.size() > 0) {
      for (ControlCodeFullView child : children) {
        PrototypeTreeNode<ControlCodeFullView> childNode = node.addChild(child);
        recursiveFunc(result, childNode);
      }
    }
    return node;
  }

  public Result renderForm(String controlCodeParam) throws ExecutionException, InterruptedException {
    PrototypeControlCodeForm templateForm = new PrototypeControlCodeForm();
    return renderWithForm(controlCodeParam, formFactory.form(PrototypeControlCodeForm.class).fill(templateForm));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<PrototypeControlCodeForm> form = formFactory.form(PrototypeControlCodeForm.class).bindFromRequest();
    return null;
  }

  public static class PrototypeControlCodeForm {
//    @Required(message = "Select an option")
//    public String option;
  }

}
