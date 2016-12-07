package importcontent.models;

import utils.common.SelectOption;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ImportWhat {

  FIREARMS("Firearms and ammunition"),
  EXPLOSIVES("Explosives"),
  LAND_MINES("Anti-personnel and land mines"),
  TORTURE("Goods that could be used for torture"),
  NUCLEAR("Nuclear materials"),
  CHEMICALS("Chemicals, pesticides and ozone-depleting substances"),
  MEDICINES("Medicines, pharmaceuticals and drugs"),
  DIAMONDS("Diamonds"),
  IRON("Iron and steel"),
  FOOD("Food, animals and plants"),
  TEXTILES("Textiles and clothing"),
  NONE_ABOVE("None of the above");

  private String prompt;

  ImportWhat(String prompt) {
    this.prompt = prompt;
  }

  public String getPrompt() {
    return this.prompt;
  }

  public boolean equals(String arg) {
    return this.prompt.equals(arg);
  }

  public static List<SelectOption> getSelectOptions() {
    List<ImportWhat> enums = Arrays.asList(ImportWhat.values());
    return enums.stream().map(e -> new SelectOption(e.name(), e.getPrompt())).collect(Collectors.toList());
  }

  public static Optional<ImportWhat> getMatched(String name) {
    return EnumSet.allOf(ImportWhat.class).stream().filter(e -> e.name().equals(name)).findFirst();
  }

}
