package models.view.licencefinder;

public class Customer {

  private String id;
  private String companyName;

  public Customer() {}

  public Customer(String id, String companyName) {
    this.id = id;
    this.companyName = companyName;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }
}
