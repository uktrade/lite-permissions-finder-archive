package controllers.search;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import model.ExportCategory;
import play.mvc.Result;
import views.html.search.noneDescribed;

public class NoneDescribedController {

  private final PermissionsFinderDao dao;

  @Inject
  public NoneDescribedController(PermissionsFinderDao dao) {
    this.dao = dao;
  }

  public Result render() {
    boolean showFirearmsOrMilitary = dao.getExportCategory() == ExportCategory.MILITARY;
    return ok(noneDescribed.render(showFirearmsOrMilitary));
  }
}
