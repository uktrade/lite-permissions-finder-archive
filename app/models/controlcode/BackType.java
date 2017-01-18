package models.controlcode;

import java.util.Optional;

public enum BackType {
  SEARCH,
  RESULTS,
  MATCHES,
  EXPORT_CATEGORY,
  SOFT_TECH_CATEGORY;

  public static Optional<BackType> getMatched(String name) {
    try {
      return Optional.of(BackType.valueOf(name));
    }
    catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }
}
