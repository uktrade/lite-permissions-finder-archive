package controllers.search;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import model.ExportCategory;
import play.mvc.Result;
import views.html.search.noneDescribed;

import java.util.Optional;

public class NoneDescribedController {

  private final PermissionsFinderDao dao;

  @Inject
  public NoneDescribedController(PermissionsFinderDao dao) {
    this.dao = dao;
  }

  public Result render() {
    Optional<ExportCategory> exportCategoryOptional = dao.getExportCategory();
    boolean showFirearmsOrMilitary = exportCategoryOptional.isPresent() && exportCategoryOptional.get() == ExportCategory.MILITARY;
    return ok(noneDescribed.render(showFirearmsOrMilitary));
  }
}
