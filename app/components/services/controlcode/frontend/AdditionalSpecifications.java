package components.services.controlcode.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AdditionalSpecifications {

  @JsonProperty("clauseText")
  public String clauseText;

  @JsonProperty("specificationText")
  public List<SpecificationText> specificationText;

  public AdditionalSpecifications(){}

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
