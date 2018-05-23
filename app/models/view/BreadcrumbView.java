package models.view;

import java.util.List;

public class BreadcrumbView {

  private final List<BreadcrumbItemView> breadcrumbItemViews;
  private final List<NoteView> noteViews;
  private final boolean decontrol;

  public BreadcrumbView(List<BreadcrumbItemView> breadcrumbItemViews, List<NoteView> noteViews, boolean decontrol) {
    this.breadcrumbItemViews = breadcrumbItemViews;
    this.noteViews = noteViews;
    this.decontrol = decontrol;
  }

  public List<BreadcrumbItemView> getBreadcrumbItemViews() {
    return breadcrumbItemViews;
  }

  public List<NoteView> getNoteViews() {
    return noteViews;
  }

  public boolean isDecontrol() {
    return decontrol;
  }

}
