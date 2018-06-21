package models.view;

import java.util.List;

public class BreadcrumbView {

  private final List<BreadcrumbItemView> breadcrumbItemViews;
  private final List<NoteView> noteViews;

  public BreadcrumbView(List<BreadcrumbItemView> breadcrumbItemViews, List<NoteView> noteViews) {
    this.breadcrumbItemViews = breadcrumbItemViews;
    this.noteViews = noteViews;
  }

  public List<BreadcrumbItemView> getBreadcrumbItemViews() {
    return breadcrumbItemViews;
  }

  public List<NoteView> getNoteViews() {
    return noteViews;
  }

}
