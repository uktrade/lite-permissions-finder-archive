package models.persistence;

public class PersistedAddress {

  private String line1;

  private String line2;

  private String town;

  private String county;

  private String postcode;

  private String country;

  private String fullAddress;

  public String getLine1() {
    return line1;
  }

  public PersistedAddress setLine1(String line1) {
    this.line1 = line1;
    return this;
  }

  public String getLine2() {
    return line2;
  }

  public PersistedAddress setLine2(String line2) {
    this.line2 = line2;
    return this;
  }

  public String getTown() {
    return town;
  }

  public PersistedAddress setTown(String town) {
    this.town = town;
    return this;
  }

  public String getCounty() {
    return county;
  }

  public PersistedAddress setCounty(String county) {
    this.county = county;
    return this;
  }

  public String getPostcode() {
    return postcode;
  }

  public PersistedAddress setPostcode(String postcode) {
    this.postcode = postcode;
    return this;
  }

  public String getCountry() {
    return country;
  }

  public PersistedAddress setCountry(String country) {
    this.country = country;
    return this;
  }

  public String getFullAddress() {
    return fullAddress;
  }

  public PersistedAddress setFullAddress(String fullAddress) {
    this.fullAddress = fullAddress;
    return this;
  }
}
