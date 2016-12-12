package importcontent.models;


import java.util.EnumSet;
import java.util.Optional;

public enum ImportCountry {

  CTRY2004("Somalia", false),
  CTRY617("Syria", false),
  CTRY220("Russia", false),
  CTRY1952("Iran", false),
  CTRY1989("Myanmar/Burma", false),
  CTRY706("Kazakhstan", false),
  CTRY31("Belarus", false),
  CTRY383("North Korea", false),
  CTRY781("Austria", true),
  CTRY485("Belgium", true),
  CTRY1123("Bulgaria", true),
  CTRY72("Croatia", true),
  CTRY1531("Republic of Cyprus", true),
  CTRY1576("Czech Republic", true),
  CTRY1364("Denmark", true),
  CTRY1581("Estonia", true),
  CTRY815("Finland", true),
  CTRY1434("France", true),
  CTRY104("Germany", true),
  CTRY1308("Greece", true),
  CTRY1746("Hungary", true),
  CTRY131("Ireland", true),
  CTRY836("Italy", true),
  CTRY958("Latvia", true),
  CTRY158("Lithuania", true),
  CTRY562("Luxembourg", true),
  CTRY1071("Malta", true),
  CTRY191("Netherlands", true),
  CTRY212("Poland", true),
  CTRY425("Portugal", true),
  CTRY1756("Romania", true),
  CTRY1859("Slovakia", true),
  CTRY1876("Slovenia", true),
  CTRY250("Spain", true),
  CTRY615("Sweden", true);

  private String description;
  private boolean euCountry;

  ImportCountry(String description, boolean euCountry) {
    this.description = description;
    this.euCountry = euCountry;
  }

  public static Optional<ImportCountry> getMatched(String name) {
    return EnumSet.allOf(ImportCountry.class).stream().filter(e -> e.name().equals(name.toUpperCase())).findFirst();
  }

  public boolean isEu() {
    return euCountry;
  }

}
