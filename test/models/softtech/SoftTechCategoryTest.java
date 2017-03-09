package models.softtech;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.util.Optional;

public class SoftTechCategoryTest {
  private static String aerospace = "AEROSPACE";
  private static String aerospaceUrl = "aerospace";

  @Test
  public void shouldMatchUpperCase() {
    Optional<SoftTechCategory> matched = SoftTechCategory.getMatched(aerospace);
    assertThat(matched.isPresent()).isTrue();
    assertThat(matched.get()).isEqualTo(SoftTechCategory.AEROSPACE);
    assertThat(matched.get().toUrlString()).isEqualTo(aerospaceUrl);
  }

  @Test
  public void shouldMatchLowerCase() {
    Optional<SoftTechCategory> matched = SoftTechCategory.getMatched(aerospace.toLowerCase());
    assertThat(matched.isPresent()).isTrue();
    assertThat(matched.get()).isEqualTo(SoftTechCategory.AEROSPACE);
    assertThat(matched.get().toUrlString()).isEqualTo(aerospaceUrl);
  }
}
