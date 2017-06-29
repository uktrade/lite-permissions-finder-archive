package models.summary;

import uk.gov.bis.lite.ogel.api.view.OgelFullView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Summary {

  public static class ValidatedOgel {
    public final OgelFullView ogelServiceResult;
    public final boolean isValid;

    public ValidatedOgel(OgelFullView ogelServiceResult, boolean isValid) {
      this.ogelServiceResult = ogelServiceResult;
      this.isValid = isValid;
    }
  }

  public final String applicationCode;

  public final List<SummaryField> summaryFields;

  public Summary(String applicationCode) {
    this.applicationCode = applicationCode;
    this.summaryFields = new ArrayList<>();
  }

  public Summary addSummaryField(SummaryField summaryField){
    this.summaryFields.add(summaryField);
    return this;
  }

  public Optional<SummaryField> findSummaryField(SummaryFieldType summaryFieldType) {
    if (!summaryFields.isEmpty()) {
      return summaryFields.stream().filter(field -> field.getSummaryFieldType() == summaryFieldType).findFirst();
    }
    else {
      return Optional.empty();
    }
  }

  public boolean isValid() {
    return this.summaryFields.stream().allMatch(f -> f.isValid());
  }
}
