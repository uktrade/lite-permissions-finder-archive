package importcontent.models;

import utils.common.SelectOption;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ImportYesNo aliases correspond to form submit values
 */
public enum ImportYesNo {

  YES("true"),
  NO("false");

  private String alias;

  ImportYesNo(String alias) {
    this.alias = alias;
  }

  public String getAlias() {
    return this.alias;
  }

  public static ImportYesNo fromAlias(String alias) {
    for (ImportYesNo iyn : ImportYesNo.values()) {
      if (iyn.alias.equalsIgnoreCase(alias)) {
        return iyn;
      }
    }
    return null;
  }

  public static List<SelectOption> getSelectOptions() {
    List<ImportYesNo> enums = Arrays.asList(ImportYesNo.values());
    return enums.stream().map(e -> new SelectOption(e.getAlias(), e.getAlias())).collect(Collectors.toList());
  }

  public static Optional<ImportYesNo> getMatched(String name) {
    return EnumSet.allOf(ImportYesNo.class).stream().filter(e -> e.name().equals(name)).findFirst();
  }

}
