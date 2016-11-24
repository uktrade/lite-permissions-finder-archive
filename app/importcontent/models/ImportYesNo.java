package importcontent.models;

import utils.common.SelectOption;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ImportYesNo {

  YES("Yes"),
  NO("No");

  private String prompt;

  ImportYesNo(String prompt) {
    this.prompt = prompt;
  }

  public String getPrompt() {
    return this.prompt;
  }

  public boolean equals(String arg) {
    return this.prompt.equals(arg);
  }

  public static List<SelectOption> getSelectOptions() {
    List<ImportYesNo> enums = Arrays.asList(ImportYesNo.values());
    return enums.stream().map(e -> new SelectOption(e.name(), e.getPrompt())).collect(Collectors.toList());
  }

  public static Optional<ImportYesNo> getMatched(String name) {
    return EnumSet.allOf(ImportYesNo.class).stream().filter(e -> e.name().equals(name)).findFirst();
  }

}
