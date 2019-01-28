package components.cms.parser;

import components.cms.parser.model.NavigationLevel;
import components.cms.parser.model.definition.Definition;
import lombok.Data;
import models.cms.SpreadsheetVersion;

import java.util.List;

@Data
public class ParserResult {
  private final List<NavigationLevel> navigationLevels;
  private final List<Definition> definitions;
  private final SpreadsheetVersion spreadsheetVersion;
}
