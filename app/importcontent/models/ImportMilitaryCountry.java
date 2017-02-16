package importcontent.models;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ImportMilitaryCountry {

  RUSSIA("CTRY220"),
  IRAN("CTRY1952"),
  MYANMAR("CTRY1989");

  private String code;

  ImportMilitaryCountry(String code) {
    this.code = code;
  }

  public String code() {
    return this.code;
  }

  public boolean equals(String code) {
    return this.code.equals(code);
  }

  public static Optional<ImportMilitaryCountry> getByCode(String code) {
    return EnumSet.allOf(ImportMilitaryCountry.class).stream().filter(e -> e.code().equals(code)).findFirst();
  }

  public static List<String> getImportMilitaryCountryCodes() {
    return EnumSet.allOf(ImportMilitaryCountry.class).stream().map(ImportMilitaryCountry::code).collect(Collectors.toList());
  }

}
