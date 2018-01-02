package controllers.prototype;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.data.FormFactory;
import play.mvc.Result;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;
import views.html.prototype.prototypeControlCode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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


  public Result renderForm(String controlCodeParam) throws ExecutionException, InterruptedException {

    boolean showDetail = StringUtils.isBlank(formFactory.form().bindFromRequest().get("hideDetail"));

    Map<String, ControlCodeFullView> result;
    if (cache.getCache().isEmpty()) {
      cache.load();
      result = cache.getCache();
    } else {
      result = cache.getCache();
    }

    ControlCodeFullView givenControlCode = result.get(controlCodeParam);
    PrototypeTreeNode<ControlCodeFullView> targetNode = new PrototypeTreeNode<>(givenControlCode);
    targetNode = findChildrenRecursive(result, targetNode);

    PrototypeTreeNode<ControlCodeFullView> rootNode = findParentsRecursive(result, targetNode);

    return ok(prototypeControlCode.render(rootNode, showDetail));
  }

  private PrototypeTreeNode<ControlCodeFullView> findChildrenRecursive(Map<String, ControlCodeFullView> result, PrototypeTreeNode<ControlCodeFullView> node) {
    List<ControlCodeFullView> children = result.values().stream()
        .filter(d -> d.getParentId().equals(node.data.getId()))
        .sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getDisplayOrder())))
        .collect(Collectors.toList());

    if (children.size() > 0) {
      for (ControlCodeFullView child : children) {
        PrototypeTreeNode<ControlCodeFullView> childNode = node.addChild(child);
        findChildrenRecursive(result, childNode);
      }
    }
    return node;
  }

  private PrototypeTreeNode<ControlCodeFullView> findParentsRecursive(Map<String, ControlCodeFullView> result, PrototypeTreeNode<ControlCodeFullView> node) {
    Optional<ControlCodeFullView> parent = result.values().stream()
        .filter(d -> d.getId().equals(node.data.getParentId()))
        .findFirst();

    if (parent.isPresent()) {
      PrototypeTreeNode<ControlCodeFullView> newRoot = new PrototypeTreeNode<>(parent.get());
      node.parent = newRoot;
      newRoot.children = Collections.singletonList(node);

      return findParentsRecursive(result, newRoot);
    }
    return node;
  }
}
