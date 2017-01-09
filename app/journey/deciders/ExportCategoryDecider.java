package journey.deciders;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.PermissionsFinderDao;
import models.ExportCategory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ExportCategoryDecider implements Decider<ExportCategory> {

  private final PermissionsFinderDao dao;

  @Inject
  public ExportCategoryDecider(PermissionsFinderDao dao) {
    this.dao = dao;
  }

  @Override
  public CompletionStage<ExportCategory> decide() {

    ExportCategory exportCategory = dao.getExportCategory().get();

    return CompletableFuture.completedFuture(exportCategory);
  }
}
