package journey.deciders.importcontent;

import com.google.inject.Inject;
import components.common.journey.Decider;
import components.persistence.ImportJourneyDao;
import importcontent.models.ImportMilitaryCountry;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ImportMilitaryDecider implements Decider<ImportMilitaryCountry> {

  private final ImportJourneyDao dao;

  @Inject
  public ImportMilitaryDecider(ImportJourneyDao dao) {
    this.dao = dao;
  }

  @Override
  public CompletionStage<ImportMilitaryCountry> decide() {
    String country = dao.getImportCountrySelected();
    Optional<ImportMilitaryCountry> importMilitaryYesNo = ImportMilitaryCountry.getByCode(country);
    return CompletableFuture.completedFuture(importMilitaryYesNo.get());
  }
}
