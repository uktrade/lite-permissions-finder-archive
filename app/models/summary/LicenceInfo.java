package models.summary;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LicenceInfo {
  private @Getter @Setter String licenceNumber;
  private @Getter @Setter String licenceType;
  private @Getter @Setter String registrationDate;
  private @Getter @Setter String companyName;
  private @Getter @Setter String companyNumber;
  private @Getter @Setter String siteName;
  private @Getter @Setter String siteAddress;
  private @Getter @Setter String customerId;
  private @Getter @Setter String siteId;
  private @Getter @Setter String ogelType;
  private @Getter @Setter String licenceUrl;

  public String getFormattedRegistrationDate() {
    LocalDate date = LocalDate.parse(registrationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
  }
}
