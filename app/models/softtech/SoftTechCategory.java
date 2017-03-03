package models.softtech;

import com.google.common.base.Enums;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public enum SoftTechCategory {
  AEROSPACE("aerospace", "Propulsion systems"),
  COMPUTERS("computers", "Computers"),
  ELECTRONICS("electronics", "Other electronic equipment"),
  MARINE("marine", "Marine vehicles, vessels and non-navigation equipment"),
  MATERIALS_PROCESSING("materials-processing", "Equipment for processing materials into finished goods"),
  MILITARY("military", null),
  NAVIGATION("navigation", "Navigation systems"),
  NUCLEAR("nuclear", "Nuclear facilities, equipment and materials"),
  SENSORS("sensors", "Sensors, lasers and real-time data processing"),
  SPECIAL_MATERIALS("special-materials", "Non-nuclear materials and chemicals"),
  TELECOMS("telecoms", "Telecommunications and information security"),
  DUAL_USE_UNSPECIFIED(null, null); // Used following a "NONE MATCHED" route

  private String heading;
  private String urlString;

  SoftTechCategory(String heading, String urlString) {
    this.heading = heading;
    this.urlString = urlString;
  }

  public String toUrlString() {
    return this.urlString;
  }

  public static boolean isDualUseSoftTechCategory(SoftTechCategory softTechCategory) {
    return softTechCategory != MILITARY;
  }

  public boolean isDualUseSoftTechCategory() {
    return isDualUseSoftTechCategory(this);
  }

  public String getHeading() {
    return this.heading;
  }

  public static Optional<SoftTechCategory> getMatched(String name) {
    if (StringUtils.isEmpty(name)) {
      return Optional.empty();
    }
    else {
      return Enums.getIfPresent(SoftTechCategory.class, name)
          .transform(java.util.Optional::of)
          .or(java.util.Optional.empty());
    }
  }

}
