package components.cms.parser.model.navigation.column;

public class OnPageContent {
  private final String title;
  private final String explanatoryNotes;

  public OnPageContent(String title, String explanatoryNotes) {
    this.title = title;
    this.explanatoryNotes = explanatoryNotes;
  }

  public String getTitle() {
    return title;
  }

  public String getExplanatoryNotes() {
    return explanatoryNotes;
  }
}

