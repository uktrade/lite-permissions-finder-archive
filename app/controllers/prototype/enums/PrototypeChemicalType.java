package controllers.prototype.enums;

import java.util.HashMap;
import java.util.Map;

public enum PrototypeChemicalType {

  ML7a("biological agents or radioactive materials adapted for use in war to produce casualties in animals or humans, damage crops or the environment or degrade equipment"),
  ML7b("chemical warfare [CW] agents"),
  ML7c("chemical warfare [CW] binary and key precursors and chemical mixtures"),
  ML7d("riot control agents, active constituent chemicals and combinations"),
  ML7e("equipment specially designed or modified for military use, designed or modified for the dissemination of any of the following, and specially designed components"),
  ML7f("Protective and decontamination goods, specially designed or modified for military use, components and chemical mixtures"),
  ML7g("Goods specially designed or modified for military use, designed or modified for the detection or identification of materials specified in ML7.a., ML7.b. or ML7.d. and specially designed components"),
  ML7h("biopolymers specially designed or processed for the detection or identification of CW agents specified in ML7.b. and the cultures of specific cells used to produce them"),
  ML7i("biocatalysts for the decontamination or degradation of CW agents and biological systems");

  private String value;
  // Reverse-lookup map
  private static final Map<String, PrototypeChemicalType> lookup = new HashMap<>();

  static {
    for (PrototypeChemicalType d : PrototypeChemicalType.values()) {
      lookup.put(d.value(), d);
    }
  }

  PrototypeChemicalType(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public static PrototypeChemicalType get(String value) {
    return lookup.get(value);
  }

}
