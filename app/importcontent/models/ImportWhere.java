package importcontent.models;


import utils.common.SelectOption;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ImportWhere {

  CRIMEA("Crimea"),
  EU("EU"),
  SOMALIA("Somalia"),
  SYRIA("Syria"),
  RUSSIA("Russia"),
  IRAN("Iran"),
  MYANMAR("Myanmar"),
  OTHER("Other");

  private String prompt;

  ImportWhere(String prompt) {
    this.prompt = prompt;
  }

  public String getPrompt() {
    return this.prompt;
  }

  public boolean equals(String arg) {
    return this.prompt.equals(arg);
  }

  public static List<SelectOption> getSelectOptions() {
    List<ImportWhere> enums = Arrays.asList(ImportWhere.values());
    return enums.stream().map(e -> new SelectOption(e.name(), e.getPrompt())).collect(Collectors.toList());
  }

  public static Optional<ImportWhere> getMatched(String name) {
    return EnumSet.allOf(ImportWhere.class).stream().filter(e -> e.name().equals(name)).findFirst();
  }

}
