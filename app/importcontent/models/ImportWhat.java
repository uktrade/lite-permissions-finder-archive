package importcontent.models;

import utils.common.SelectOption;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ImportWhat {

  FIREARMS("Firearms and ammunition", true),
  EXPLOSIVES("Explosives", true),
  LAND_MINES("Anti-personnel and land mines", true),
  TORTURE("Goods that could be used for torture", true),
  NUCLEAR("Nuclear materials", true),
  CHEMICALS("Chemicals, pesticides and ozone-depleting substances", true),
  MEDICINES("Medicines, pharmaceuticals and drugs", true),
  DIAMONDS("Diamonds", true),
  IRON("Iron or steel", true),
  IRON_KAZAKHSTAN("Iron or steel from Kazakhstan", false),
  FOOD("Food, animals and plants", true),
  TEXTILES_NORTH_KOREA("Textiles and clothing from North Korea", false),
  TEXTILES_BELARUS("Textiles and clothing from Belarus", false),
  TEXTILES("Textiles and clothing", true),
  NONE_ABOVE("None of the above", true);

  private String prompt;
  private boolean includeAsOption;

  ImportWhat(String prompt, boolean includeAsOption) {
    this.prompt = prompt;
    this.includeAsOption = includeAsOption;
  }

  public String getPrompt() {
    return this.prompt;
  }

  public boolean getIncludeAsOption() {
    return this.includeAsOption;
  }

  public boolean equals(String arg) {
    return this.prompt.equals(arg);
  }

  public static List<SelectOption> getSelectOptions() {
    List<ImportWhat> enums = Arrays.asList(ImportWhat.values());
    return enums.stream()
        .filter(ImportWhat::getIncludeAsOption)
        .map(e -> new SelectOption(e.name(), e.getPrompt())).collect(Collectors.toList());
  }

  public static Optional<ImportWhat> getMatched(String name) {
    return EnumSet.allOf(ImportWhat.class).stream().filter(e -> e.name().equals(name)).findFirst();
  }

}
