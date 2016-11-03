package models.controlcode;

import components.services.controlcode.AdditionalSpecifications;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceResult;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AdditionalSpecificationsDisplay {

  public final String title;

  public final String friendlyDescription;

  public final String controlCodeAlias;

  public final String clauseText;

  public final List<String> specifications;

  public AdditionalSpecificationsDisplay(FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    AdditionalSpecifications additionalSpecifications = frontendServiceResult.controlCodeData.additionalSpecifications;
    if (additionalSpecifications != null) {
      this.clauseText = additionalSpecifications.clauseText;
      if (additionalSpecifications.specificationText != null) {
        this.specifications = additionalSpecifications.specificationText.stream().map(t -> t.text).collect(Collectors.toList());
      }
      else {
        this.specifications = Collections.emptyList();
      }
    }
    else {
      this.clauseText = null;
      this.specifications = Collections.emptyList();
    }

  }

}
