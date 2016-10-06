package components.services.ogels.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import models.summary.Summary;
import models.summary.SummaryField;
import models.summary.SummaryFieldType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
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

  public OgelRegistrationServiceRequest(String transactionId, Summary summary) {
    // Find OGEL ID, throw RuntimeException if not found
    Optional<SummaryField> fieldsummaryFieldOptional = summary.findSummaryField(SummaryFieldType.OGEL_TYPE);
    if (!fieldsummaryFieldOptional.isPresent() || StringUtils.isBlank(fieldsummaryFieldOptional.get().data)) {
      throw new RuntimeException("Attempted to build OgelRegistrationServiceRequest object without an OGEL ID");
    }
    this.transactionId = transactionId;
    this.transactionData = new TransactionData(fieldsummaryFieldOptional.get().data);
    this.editSummaryFields = summaryToFieldsList(summary);
  }

  public static List<EditSummaryField> summaryToFieldsList(Summary summary) {
    return summary.summaryFields.stream()
        .map(summaryField -> EditSummaryField.buildEditSummaryField(summaryField))
        .collect(Collectors.toList());
  }

}