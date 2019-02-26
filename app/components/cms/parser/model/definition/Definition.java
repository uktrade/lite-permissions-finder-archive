package components.cms.parser.model.definition;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Definition {
  private int rowNumber;
  private String name;
  private String list;
  private String newContent;
}
