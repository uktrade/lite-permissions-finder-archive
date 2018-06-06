package models.view;

import java.util.List;

public class BreadcrumbView {

  private final List<BreadcrumbItemView> breadcrumbItemViews;
  private final List<NoteView> noteViews;
  private final boolean decontrol;
  private final String decontrolUrl;

  public BreadcrumbView(List<BreadcrumbItemView> breadcrumbItemViews, List<NoteView> noteViews, boolean decontrol,
                        String decontrolUrl) {
    this.breadcrumbItemViews = breadcrumbItemViews;
    this.noteViews = noteViews;
    this.decontrol = decontrol;
    this.decontrolUrl = decontrolUrl;
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

  public String getDecontrolUrl() {
    return decontrolUrl;
  }

}
