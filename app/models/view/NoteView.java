package models.view;

public class NoteView {
  private final String text;

  public NoteView(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
}
