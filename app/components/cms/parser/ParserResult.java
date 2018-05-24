package components.cms.parser;

import components.cms.parser.model.NavigationLevel;
import components.cms.parser.model.definition.Definition;

import java.util.List;

public class ParserResult {
  private final List<NavigationLevel> navigationLevels;
  private final List<Definition> definitions;

  public ParserResult(List<NavigationLevel> navigationLevels, List<Definition> definitions) {
    this.navigationLevels = navigationLevels;
    this.definitions = definitions;
  }

  public List<NavigationLevel> getNavigationLevels() {
    return navigationLevels;
  }

  public List<Definition> getDefinitions() {
    return definitions;
  }
}
