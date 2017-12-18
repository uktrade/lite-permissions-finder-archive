package controllers.search.enums;

import utils.common.SelectOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum GoodsSpecialisation {

  DESIGNED("Yes - specially designed"),
  MODIFIED("Yes - specially modified"),
  NONE("No");

  private String prompt;

  GoodsSpecialisation(String prompt) {
    this.prompt = prompt;
  }

  public String getPrompt() {
    return prompt;
  }

  public static List<SelectOption> getSelectOptions() {
    return Arrays.stream(GoodsSpecialisation.values())
        .map(e -> new SelectOption(e.name(), e.getPrompt())).collect(Collectors.toList());
  }

}
