package models.summary;

import java.util.ArrayList;
import java.util.List;

public class Summary {

  public final List<SummaryField> summaryFields;

  public Summary() {
    this.summaryFields = new ArrayList<>();
  }

  public Summary addSummaryField(SummaryField summaryField){
    this.summaryFields.add(summaryField);
    return this;
  }
  
}
