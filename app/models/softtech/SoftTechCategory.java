package models.softtech;

import com.google.common.base.Enums;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public enum SoftTechCategory {
  AEROSPACE("Propulsion systems"),
  COMPUTERS("Computers"),
  ELECTRONICS("Other electronic equipment"),
  MARINE("Marine vehicles, vessels and non-navigation equipment"),
  MATERIALS_PROCESSING("Equipment for processing materials into finished goods"),
  MILITARY(null),
  NAVIGATION("Navigation systems"),
  NUCLEAR("Nuclear facilities, equipment and materials"),
  SENSORS("Sensors, lasers and real-time data processing"),
  SPECIAL_MATERIALS("Non-nuclear materials and chemicals"),
  TELECOMS("Telecommunications and information security"),
  DUAL_USE_UNSPECIFIED(null); // Used following a "NONE MATCHED" route

  private String heading;

  SoftTechCategory(String heading) {
    this.heading = heading;
  }

  public String toUrlString() {
    return this.toString().replace('_','-').toLowerCase();
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
