package components.cms.parser.model;

import components.cms.parser.model.navigation.column.Buttons;
import components.cms.parser.model.navigation.column.ControlListEntries;
import components.cms.parser.model.navigation.column.Decontrols;
import components.cms.parser.model.navigation.column.Definitions;
import components.cms.parser.model.navigation.column.Loops;
import components.cms.parser.model.navigation.column.Nesting;
import components.cms.parser.model.navigation.column.Notes;
import components.cms.parser.model.navigation.column.OnPageContent;
import components.cms.parser.model.navigation.column.Redirect;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;

@Data
public class NavigationLevel {
  private final String cellAddress;
  private final String content;
  private final int level;
  private final String list;
  private final String friendlyName;
  private final ArrayList<NavigationLevel> subNavigationLevels;
  private final OnPageContent onPageContent;
  private final ControlListEntries controlListEntries;
  private final Buttons buttons;
  private final Nesting nesting;
  private final Loops loops;
  private final Decontrols decontrols;
  private final Definitions definitions;
  private final Notes notes;
  private final Redirect redirect;
  private final LoadingMetadata loadingMetadata;

  public NavigationLevel(String cellAddress, String content, int level, String list, String friendlyName) {
    this(cellAddress, content, level, list, friendlyName, null, null, null, null, null, null, null, null, Redirect.NONE);
  }

  public NavigationLevel(
      String cellAddress,
      String content,
      int level,
      String list,
      String friendlyName,
      OnPageContent onPageContent,
      ControlListEntries controlListEntries,
      Buttons buttons,
      Nesting nesting,
      Loops loops,
      Decontrols decontrols,
      Definitions definitions,
      Notes notes,
      Redirect redirect
  ) {
    this.cellAddress = cellAddress;
    this.content = content;
    this.level = level;
    this.list = list;
    this.friendlyName = friendlyName;
    this.onPageContent = onPageContent;
    this.controlListEntries = controlListEntries;
    this.buttons = buttons;
    this.nesting = nesting;
    this.loops = loops;
    this.decontrols = decontrols;
    this.definitions = definitions;
    this.notes = notes;
    this.redirect = redirect;
    loadingMetadata = new LoadingMetadata();
    subNavigationLevels = new ArrayList<>();
  }

  public void addSubNavigationLevel(NavigationLevel navigationLevel) {
    subNavigationLevels.add(navigationLevel);
  }

  public void addAllSubNavigationLevels(Collection<NavigationLevel> navigationLevels) {
    subNavigationLevels.addAll(navigationLevels);
  }
}
