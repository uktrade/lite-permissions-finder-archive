package controllers.services.controlcode.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AdditionalSpecifications {

  @JsonProperty("clauseText")
  public String clauseText;

  @JsonProperty("specificationText")
  public List<SpecificationText> specificationText;

  public AdditionalSpecifications(){}

}
