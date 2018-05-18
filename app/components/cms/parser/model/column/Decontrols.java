package components.cms.parser.model.column;

public class Decontrols {
  private final String content;
  private final String note;

  public Decontrols(String content, String note) {
    this.content = content;
    this.note = note;
  }

  public String getContent() {
    return content;
  }

  public String getNote() {
    return note;
  }
}
