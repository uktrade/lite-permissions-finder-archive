package components.cms.parser.model;

import components.cms.parser.model.navigation.column.Breadcrumbs;
import components.cms.parser.model.navigation.column.Buttons;
import components.cms.parser.model.navigation.column.ControlListEntries;
import components.cms.parser.model.navigation.column.Decontrols;
import components.cms.parser.model.navigation.column.Definitions;
import components.cms.parser.model.navigation.column.Loops;
import components.cms.parser.model.navigation.column.NavigationExtras;
import components.cms.parser.model.navigation.column.Nesting;
import components.cms.parser.model.navigation.column.Notes;
import components.cms.parser.model.navigation.column.OnPageContent;
import components.cms.parser.model.navigation.column.Redirect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NavigationLevel {
  private final String cellAddress;
  private final String content;
  private final int level;
  private final String list;
  private final ArrayList<NavigationLevel> subNavigationLevels;
  private final NavigationExtras navigationExtras;
  private final OnPageContent onPageContent;
  private final ControlListEntries controlListEntries;
  private final Buttons buttons;
  private final Nesting nesting;
  private final Loops loops;
  private final Breadcrumbs breadcrumbs;
  private final Decontrols decontrols;
  private final Definitions definitions;
  private final Notes notes;
  private final Redirect redirect;
  private final LoadingMetadata loadingMetadata;

  public NavigationLevel(String cellAddress, String content, int level, String list) {
    this(cellAddress, content, level, list, null, null, null, null, null, null, null, null, null, null, null);
  }

  public NavigationLevel(
      String cellAddress,
      String content,
      int level,
      String list,
      NavigationExtras navigationExtras,
      OnPageContent onPageContent,
      ControlListEntries controlListEntries,
      Buttons buttons,
      Nesting nesting,
      Loops loops,
      Breadcrumbs breadcrumbs,
      Decontrols decontrols,
      Definitions definitions,
      Notes notes,
      Redirect redirect
  ) {
    this.cellAddress = cellAddress;
    this.content = content;
    this.level = level;
    this.list = list;
    this.navigationExtras = navigationExtras;
    this.onPageContent = onPageContent;
    this.controlListEntries = controlListEntries;
    this.buttons = buttons;
    this.nesting = nesting;
    this.loops = loops;
    this.breadcrumbs = breadcrumbs;
    this.decontrols = decontrols;
    this.definitions = definitions;
    this.notes = notes;
    this.redirect = redirect;
    loadingMetadata = new LoadingMetadata();
    subNavigationLevels = new ArrayList<>();
  }

  @Override
  public String toString() {
    return "NavigationLevel{" +
        "cellAddress='" + cellAddress + '\'' +
        ",\n content='" + content + '\'' +
        ",\n level=" + level +
        '}';
  }

  public String getCellAddress() {
    return cellAddress;
  }

  public String getContent() {
    return content;
  }

  public int getLevel() {
    return level;
  }

  public List<NavigationLevel> getSubNavigationLevels() {
    return subNavigationLevels;
  }

  public NavigationExtras getNavigationExtras() {
    return navigationExtras;
  }

  public OnPageContent getOnPageContent() {
    return onPageContent;
  }

  public ControlListEntries getControlListEntries() {
    return controlListEntries;
  }

  public Buttons getButtons() {
    return buttons;
  }

  public Nesting getNesting() {
    return nesting;
  }

  public Loops getLoops() {
    return loops;
  }

  public Breadcrumbs getBreadcrumbs() {
    return breadcrumbs;
  }

  public Decontrols getDecontrols() {
    return decontrols;
  }

  public Definitions getDefinitions() {
    return definitions;
  }

  public Notes getNotes() {
    return notes;
  }

  public Redirect getRedirect() {
    return redirect;
  }

  public String getList() {
    return list;
  }

  public void addSubNavigationLevel(NavigationLevel navigationLevel) {
    subNavigationLevels.add(navigationLevel);
  }

  public void addAllSubNavigationLevels(Collection<NavigationLevel> navigationLevels) {
    subNavigationLevels.addAll(navigationLevels);
  }

  public LoadingMetadata getLoadingMetadata() {
    return loadingMetadata;
  }
}
