package models.softtech;

import static models.GoodsType.SOFTWARE;

import controllers.softtech.routes;
import models.GoodsType;

import java.util.HashMap;
import java.util.Map;

public class DualUseSoftTechCategoriesDisplay {
  public final String formAction;
  public final String pageTitle;
  private final Map<SoftTechCategory, String> contentMap;

  public DualUseSoftTechCategoriesDisplay(GoodsType goodsType) {
    this.contentMap = new HashMap<>();
    if (goodsType == GoodsType.SOFTWARE) {
      this.formAction = routes.DualUseSoftTechCategoriesController.handleSubmit(goodsType.urlString()).url();
      this.pageTitle = "What is your software for?";
    }
    else if (goodsType == GoodsType.TECHNOLOGY) {
      this.formAction = routes.DualUseSoftTechCategoriesController.handleSubmit(goodsType.urlString()).url();
      this.pageTitle = "What is your technology for?";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\""
          , goodsType.toString()));
    }
    String softwareOrTechnicalInformation = goodsType == SOFTWARE ? "software" : "technical information";
    String softwareOrTechnology = goodsType.value().toLowerCase();
    contentMap.put(SoftTechCategory.AEROSPACE, "This includes " + softwareOrTechnicalInformation + " for engines, space " +
        "launch vehicles and spacecraft, re-entry vehicles, liquid, solid and hybrid rocket propulsion systems, unmanned " +
        "aerial vehicles and airships, hybrid rocket motors, and launch support equipment.");
    contentMap.put(SoftTechCategory.COMPUTERS, "This includes " + softwareOrTechnicalInformation + " for electronic " +
        "computers, digital computers, systolic array computers, neural computers and optical computers, as well as" +
        (goodsType == SOFTWARE ? " " : " technology related to ") +"intrusion software.");
    contentMap.put(SoftTechCategory.ELECTRONICS, "This includes " + softwareOrTechnology + " for recording equipment and " +
        "oscilloscopes, acoustic wave devices, high energy devices, encoders, thyristor devices and modules, " +
        "semiconductor devices, signal analysers or generators, network analysers, microwave test receivers, thermal " +
        "management systems, accelerators, frequency changers or generators, and devices and circuits containing " +
        "superconductive materials.");
    contentMap.put(SoftTechCategory.MARINE, "This includes " + softwareOrTechnicalInformation + " for submersible " +
        "vehicles and surface vessels, marine systems and water tunnels.");
    contentMap.put(SoftTechCategory.MATERIALS_PROCESSING, "This is " + softwareOrTechnicalInformation + " for equipment " +
        "used in the manufacture of raw materials into finished goods, such as machine tools and assemblies, isostatic " +
        "presses, spin-forming and flow-forming machines, balancing machines and anti-friction bearing systems.");
    contentMap.put(SoftTechCategory.NAVIGATION, "This includes " + softwareOrTechnicalInformation + " for flight " +
        "control systems, altimeters, Global Navigation Satellite System receiving equipment, accelerometers, gyros, " +
        "inertial measurement equipment, star trackers, underwater sonar navigation systems, and integration software " +
        "for navigation and avionics equipment.");
    contentMap.put(SoftTechCategory.NUCLEAR, "This includes " + softwareOrTechnicalInformation + " for equipment used " +
        "in connection with nuclear reactors, thorium, deuterium, plutonium, uranium, depleted uranium or special " +
        "fissile materials, nuclear reactor fuel elements, and auxiliary systems, equipment and components for isotope " +
        "separation plant.");
    contentMap.put(SoftTechCategory.SENSORS, "This includes " + softwareOrTechnology + " for optical equipment, lasers, " +
        "radar systems, monospectral imaging sensors, multispectral imaging sensors, real-time data processing " +
        "equipment, cameras, magnetic sensors, gravity meters or gradiometers, and Air Traffic Control software.");
    contentMap.put(SoftTechCategory.SPECIAL_MATERIALS, "This is " + softwareOrTechnology + " for equipment used to " +
        "develop or produce non-nuclear materials and chemicals, including composite structures or laminates, fibrous " +
        "or filamentary materials, metal alloys, metal alloy powder, alloyed materials, superplastic forming or " +
        "diffusion bonding, explosive residue detection and analysis of reduced observables.");
    contentMap.put(SoftTechCategory.TELECOMS, "This includes " + softwareOrTechnicalInformation + " for communications " +
        "systems, signal processors, radio receivers, interception, jamming and monitoring equipment, location " +
        "detection and tracking systems, surveillance systems, and equipment to ensure the security of information or " +
        "communications, including cryptography, cryptographic activation and cryptanalysis.");
  }

  public String getContent(SoftTechCategory softTechCategory) {
    return contentMap.get(softTechCategory);
  }

}