package models;

import utils.common.SelectOption;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ExportYesNo {

  YES("Yes"),
  NO("No");

  private String prompt;

  ExportYesNo(String prompt) {
    this.prompt = prompt;
  }

  public String getPrompt() {
    return this.prompt;
  }

  public static List<SelectOption> getSelectOptions() {
    List<ExportYesNo> enums = Arrays.asList(ExportYesNo.values());
    return enums.stream().map(e -> new SelectOption(e.name(), e.getPrompt())).collect(Collectors.toList());
  }

  public static Optional<ExportYesNo> getMatched(String name) {
    return EnumSet.allOf(ExportYesNo.class).stream().filter(e -> e.name().equals(name)).findFirst();
  }
}
