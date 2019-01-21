package models.view;

import lombok.Data;

import java.util.List;

@Data
public class BreadcrumbView {
  private final List<BreadcrumbItemView> breadcrumbItemViews;
  private final List<NoteView> noteViews;
}
