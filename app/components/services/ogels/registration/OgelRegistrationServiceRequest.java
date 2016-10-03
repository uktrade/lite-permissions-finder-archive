package components.services.ogels.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import components.services.controlcode.frontend.ControlCodeData;
import components.services.ogels.ogel.OgelServiceResult;
import models.common.Country;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OgelRegistrationServiceRequest {

  @JsonProperty("transactionId")
  public final String transactionId;

  @JsonProperty("transactionType")
  public final String transactionType = "OGEL_REGISTRATION";

  @JsonProperty("transactionData")
  public final TransactionData transactionData;

  @JsonProperty("editSummaryFields")
  public final List<EditSummaryField> editSummaryFields;

  public OgelRegistrationServiceRequest(String transactionId, OgelServiceResult ogel,
                                        List<Country> destinationCountries, ControlCodeData controlCode) {
    this.transactionId = transactionId;
    this.transactionData = new TransactionData(ogel.id);
    this.editSummaryFields = new ArrayList<>();
    this.editSummaryFields.add(new EditSummaryField("Goods rating", "<strong class=\"bold-small\">" +
        controlCode.controlCode + "</strong> " + controlCode.title, "#")); // TODO editLink
    this.editSummaryFields.add(new EditSummaryField("Licence type", ogel.name, "#")); // TODO editLink
    this.editSummaryFields.add(new EditSummaryField("Destination Countries",
        destinationCountries.stream().map(c -> c.getCountryName()).collect(Collectors.joining(", ")), "#")); // TODO editLink
  }

}