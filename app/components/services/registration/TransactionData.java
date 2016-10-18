package components.services.registration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionData {

  @JsonProperty("OGEL_TYPE")
  public final String ogelType;

  public TransactionData(String ogelType) {
    this.ogelType = ogelType;
  }

}
