package models.view;

import lombok.Data;
import java.util.List;

@Data
public class BreadcrumbItemView {
  private final String text;
  private final String description;
  private final String url;
  private final List<NoteView> noteViews;
}
