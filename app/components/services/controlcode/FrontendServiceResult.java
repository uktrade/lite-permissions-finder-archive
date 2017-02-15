package components.services.controlcode;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeSummary;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView.FrontEndControlCodeData;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FrontendServiceResult {
  private final FrontEndControlCodeView frontendControlCode;

  private final Optional<ControlCodeSummary> greatestAncestor;

  private final List<ControlCodeSummary> otherAncestors;

  public FrontendServiceResult(JsonNode responseJson) {
    this.frontendControlCode = Json.fromJson(responseJson, FrontEndControlCodeView.class);
    List<ControlCodeSummary> reversedAncestors = Lists.reverse(this.frontendControlCode.getLineage());
    this.greatestAncestor = !reversedAncestors.isEmpty() ? Optional.of(reversedAncestors.get(0)) : Optional.empty();
    this.otherAncestors = reversedAncestors.size() > 1
        ? reversedAncestors.subList(1, reversedAncestors.size())
        : Collections.emptyList();
  }

  public FrontEndControlCodeView getFrontendControlCode() {
    return this.frontendControlCode;
  }

  public FrontEndControlCodeData getControlCodeData() {
    return this.frontendControlCode.getControlCodeData();
  }

  public Optional<ControlCodeSummary> getGreatestAncestor() {
    return this.greatestAncestor;
  }

  public List<ControlCodeSummary> getOtherAncestors() {
    return this.otherAncestors;
  }

  public boolean canShowDecontrols() {
    List<FrontEndControlCodeView.FrontEndControlCodeData.Decontrol> decontrols =
        this.frontendControlCode.getControlCodeData().getDecontrols();
    return decontrols != null && !decontrols.isEmpty();
  }

  public boolean canShowTechnicalNotes() {
    return StringUtils.isNotEmpty(this.frontendControlCode.getControlCodeData().getTechnicalNotes());
  }

  public boolean canShowAdditionalSpecifications() {
    FrontEndControlCodeData.FormattedAdditionalSpecifications fas = this.frontendControlCode.getControlCodeData().getAdditionalSpecifications();
    if (fas != null) {
      boolean canShowClauseText = StringUtils.isNotEmpty(fas.getClauseText());
      boolean canShowSpecificationText = fas.getSpecificationText() != null && !fas.getSpecificationText().isEmpty();
      return canShowClauseText || canShowSpecificationText;
    }
    else {
      return false;
    }
  }
}
