package models.view;

import java.util.List;

public class BreadcrumbItemView {
  private final String text;
  private final String description;
  private final String url;
  private final List<NoteView> noteViews;

  public BreadcrumbItemView(String text, String description, String url, List<NoteView> noteViews) {
    this.text = text;
    this.description = description;
    this.url = url;
    this.noteViews = noteViews;
  }

  public String getText() {
    return text;
  }

  public String getDescription() {
    return description;
  }

  public String getUrl() {
    return url;
  }

  public List<NoteView> getNoteViews() {
    return noteViews;
  }
}
