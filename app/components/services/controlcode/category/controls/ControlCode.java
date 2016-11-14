package components.services.controlcode.category.controls;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ControlCode {
  public final String highlightedText;

  public final String code;

  public ControlCode(@JsonProperty("highlightedText") String highlightedText, @JsonProperty("code") String code) {
    this.highlightedText = highlightedText;
    this.code = code;
  }
}
