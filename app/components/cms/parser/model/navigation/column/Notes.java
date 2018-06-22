package components.cms.parser.model.navigation.column;

public class Notes {
  private final String nb;
  private final String note;
  private final String seeAlso;
  private final String techNote;
  private boolean created = false;

  public Notes(String nb, String note, String seeAlso, String techNote) {
    this.nb = nb;
    this.note = note;
    this.seeAlso = seeAlso;
    this.techNote = techNote;
  }

  public String getNb() {
    return nb;
  }

  public String getNote() {
    return note;
  }

  public String getSeeAlso() {
    return seeAlso;
  }

  public String getTechNote() {
    return techNote;
  }

  public boolean isCreated() {
    return created;
  }

  public void setCreated(boolean created) {
    this.created = created;
  }
}
