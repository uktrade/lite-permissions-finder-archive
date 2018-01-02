package controllers.prototype.enums;

public enum TriageMilEquipment {

  ML10("aircraft, lighter-than-air vehicles and unmanned aerial vehicles [UAVs]. Aero-engines and aircraft components, equipment and related goods specially designed or modified for military use"),
  ML3("ammunition, fuse setting devices and specially designed components"),
  ML13("armoured or protective goods and constructions"),
  ML4("bombs, missiles, torpedoes, rockets, other explosive devices and charges and related accessories, equipment and specially designed components"),
  ML20("cryogenic and superconductive equipment and specially designed components"),
  ML19("directed energy weapon [DEW] systems, specially designed components, countermeasures, equipment and test models"),
  ML5("fire control equipment, related alerting and warning equipment, related systems, test and alignment and countermeasure equipment specially designed for military use and specially designed components and accessories"),
  ML6("ground vehicles and components"),
  ML12("high velocity kinetic energy weapon systems, components and equipment"),
  ML15("imaging or countermeasure equipment, accessories and components"),
  ML9("naval vessels, special accessories, components and equipment"),
  PL5001("security and para-military police goods"),
  ML1("smooth-bore weapons [calibre less than 20mm] / Other weapons [calibre 12.7mm or less] - accessories and specially designed components"),
  ML2("smooth-bore weapons [calibre 20mm or more] / Other armament or weapons [calibre greater than 12.7mm] - accessories, projectors and specially designed components");

  private String value;

  TriageMilEquipment(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

}
