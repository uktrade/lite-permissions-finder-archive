package components.cms.parser.model.navigation.column;

public class Decontrols {
  private final String content;
  private final String note;
  private final String title;
  private final String explanatoryNotes;

  public Decontrols(String content, String note, String title, String explanatoryNotes) {
    this.content = content;
    this.note = note;
    this.title = title;
    this.explanatoryNotes = explanatoryNotes;
  }

  public String getContent() {
    return content;
  }

  public String getNote() {
    return note;
  }

  public String getTitle() {
    return title;
  }

  public String getExplanatoryNotes() {
    return explanatoryNotes;
  }
}
