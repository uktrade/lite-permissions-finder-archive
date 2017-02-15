package importcontent.models;

import utils.common.SelectOption;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ImportMilitaryYesNo {

  YES("Yes", true),
  YES_RUSSIA("-", false),
  YES_IRAN("-", false),
  YES_MYANMAR("-", false),
  NO("No", true);

  private String prompt;
  private boolean includeAsOption;

  ImportMilitaryYesNo(String prompt, boolean includeAsOption) {
    this.prompt = prompt;
    this.includeAsOption = includeAsOption;
  }

  public String getPrompt() {
    return this.prompt;
  }

  public boolean equals(String arg) {
    return this.prompt.equals(arg);
  }

  public boolean getIncludeAsOption() {
    return this.includeAsOption;
  }

  public static List<SelectOption> getSelectOptions() {
    List<ImportMilitaryYesNo> enums = Arrays.asList(ImportMilitaryYesNo.values());
    return enums.stream()
        .filter(ImportMilitaryYesNo::getIncludeAsOption)
        .map(e -> new SelectOption(e.name(), e.getPrompt())).collect(Collectors.toList());
  }

}
