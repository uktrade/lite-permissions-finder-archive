package importcontent.models;

import utils.common.SelectOption;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ImportWhatWhereIron {

  KAZAKHSTAN("Kazakhstan"),
  OTHER("Other");

  private String prompt;

  ImportWhatWhereIron(String prompt) {
    this.prompt = prompt;
  }

  public String getPrompt() {
    return this.prompt;
  }

  public boolean equals(String arg) {
    return this.prompt.equals(arg);
  }

  public static List<SelectOption> getSelectOptions() {
    List<ImportWhatWhereIron> enums = Arrays.asList(ImportWhatWhereIron.values());
    return enums.stream().map(e -> new SelectOption(e.name(), e.getPrompt())).collect(Collectors.toList());
  }

  public static Optional<ImportWhatWhereIron> getMatched(String name) {
    return EnumSet.allOf(ImportWhatWhereIron.class).stream().filter(e -> e.name().equals(name)).findFirst();
  }
}
