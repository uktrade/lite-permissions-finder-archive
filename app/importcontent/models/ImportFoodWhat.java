package importcontent.models;


import utils.common.SelectOption;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ImportFoodWhat {

  FOOD("Food, including animal products, fruit and vegetables"),
  NON_FOOD("Non-food animal products, such as blood, feathers, wool and manure"),
  ANIMALS("Live animals"),
  NON_EDIBLE("Non-edible plants");

  private String prompt;

  ImportFoodWhat(String prompt) {
    this.prompt = prompt;
  }

  public String getPrompt() {
    return this.prompt;
  }

  public boolean equals(String arg) {
    return this.prompt.equals(arg);
  }

  public static List<SelectOption> getSelectOptions() {
    List<ImportFoodWhat> enums = Arrays.asList(ImportFoodWhat.values());
    return enums.stream().map(e -> new SelectOption(e.name(), e.getPrompt())).collect(Collectors.toList());
  }

  public static Optional<ImportFoodWhat> getMatched(String name) {
    return EnumSet.allOf(ImportFoodWhat.class).stream().filter(e -> e.name().equals(name)).findFirst();
  }

}
