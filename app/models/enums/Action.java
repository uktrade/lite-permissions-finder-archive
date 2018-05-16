package models.enums;

public enum Action {

  NONE("none"), CONTINUE("continue");

  private final String text;

  Action(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }

}
