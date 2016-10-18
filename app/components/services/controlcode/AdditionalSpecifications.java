package components.services.controlcode;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AdditionalSpecifications {

  public final String clauseText;

  public final List<SpecificationText> specificationText;

  public AdditionalSpecifications(@JsonProperty("clauseText") String clauseText,
                                  @JsonProperty("specificationText") List<SpecificationText> specificationText) {
    this.clauseText = clauseText;
    this.specificationText = specificationText;
  }

  public boolean canShowClauseText() {
    return !(clauseText == null || clauseText.isEmpty());
  }

  public boolean canShowSpecificationText() {
    return !(specificationText == null || specificationText.isEmpty());
  }

  public boolean canShow() {
    return canShowClauseText() || canShowSpecificationText();
  }

}
