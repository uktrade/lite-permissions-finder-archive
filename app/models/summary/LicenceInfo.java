package models.summary;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LicenceInfo {
  private String licenceNumber;
  private String licenceType;
  private String registrationDate;
  private String companyName;
  private String companyNumber;
  private String siteName;
  private String siteAddress;
  private String customerId;
  private String siteId;
  private String ogelType;
  private String licenceUrl;

  public String getFormattedRegistrationDate() {
    LocalDate date = LocalDate.parse(registrationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
  }

  public String getLicenceNumber() {
    return licenceNumber;
  }

  public void setLicenceNumber(String licenceNumber) {
    this.licenceNumber = licenceNumber;
  }

  public String getLicenceType() {
    return licenceType;
  }

  public void setLicenceType(String licenceType) {
    this.licenceType = licenceType;
  }

  public String getRegistrationDate() {
    return registrationDate;
  }

  public void setRegistrationDate(String registrationDate) {
    this.registrationDate = registrationDate;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getCompanyNumber() {
    return companyNumber;
  }

  public void setCompanyNumber(String companyNumber) {
    this.companyNumber = companyNumber;
  }

  public String getSiteName() {
    return siteName;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
  }

  public String getSiteAddress() {
    return siteAddress;
  }

  public void setSiteAddress(String siteAddress) {
    this.siteAddress = siteAddress;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getSiteId() {
    return siteId;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  public String getOgelType() {
    return ogelType;
  }

  public void setOgelType(String ogelType) {
    this.ogelType = ogelType;
  }

  public String getLicenceUrl() {
    return licenceUrl;
  }

  public void setLicenceUrl(String licenceUrl) {
    this.licenceUrl = licenceUrl;
  }
}
