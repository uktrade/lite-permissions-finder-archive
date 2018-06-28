package models.view.licencefinder;

public class Site {

  private String id;
  private String address;

  public Site(String id, String address) {
    this.id = id;
    this.address = address;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}
