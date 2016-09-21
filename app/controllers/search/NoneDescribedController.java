package controllers.search;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import model.ExportCategory;
import play.mvc.Result;
import views.html.search.noneDescribed;

import java.util.Optional;

public class NoneDescribedController {

  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public NoneDescribedController(PermissionsFinderDao permissionsFinderDao) {
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result render() {
    Optional<ExportCategory> exportCategoryOptional = permissionsFinderDao.getExportCategory();
    boolean showFirearmsOrMilitary = exportCategoryOptional.isPresent() && exportCategoryOptional.get() == ExportCategory.MILITARY;
    return ok(noneDescribed.render(showFirearmsOrMilitary));
  }
}
