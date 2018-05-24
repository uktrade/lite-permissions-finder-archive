package components.cms.parser.workbook;


import components.cms.parser.ParserException;
import components.cms.parser.model.NavigationLevel;
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
import components.cms.parser.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import play.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class NavigationParser {
  private static class SheetIndices {
    static final int UK_MILITARY_LIST = 6; // Sheet 7
  }
  private static class RowIndices {
    static final int NAVIGATION_START = 3; // 4
  }
  private static class ColumnIndices {
    static class NavigationExtras {
      static final int DIV_LINE = Utils.columnToIndex("A");
    }

    static class Navigation {
      static final int START = Utils.columnToIndex("B");
      static final int END = Utils.columnToIndex("K");
    }

    static class OnPageContent {
      static final int TITLE = Utils.columnToIndex("L");
      static final int EXPLANATORY_NOTES = Utils.columnToIndex("M");
    }

    static class ControlListEntries {
      static final int RATING = Utils.columnToIndex("N");
      static final int PRIORITY = Utils.columnToIndex("O");
    }

    static class Buttons {
      static final int SELECT_ONE = Utils.columnToIndex("P");
      static final int SELECT_MANY = Utils.columnToIndex("Q");
    }

    static class Nesting {
      static final int ANY_OF = Utils.columnToIndex("R");
      static final int ALL_OF = Utils.columnToIndex("S");
    }

    static class Loops {
      static final int RELATED_CODES = Utils.columnToIndex("T");
      static final int JUMP_TO = Utils.columnToIndex("U");
      static final int DEFINING = Utils.columnToIndex("V");
      static final int DEFERRED = Utils.columnToIndex("W");
    }

    static class Breadcrumbs {
      static final int BREADCRUMB_TEXT = Utils.columnToIndex("X");
    }

    static class Decontrols {
      static final int CONTENT = Utils.columnToIndex("Y");
      static final int NOTE = Utils.columnToIndex("Z");
      static final int TITLE = Utils.columnToIndex("AA");
      static final int EXPLANATORY_NOTES = Utils.columnToIndex("AB");
    }

    static class Definitions {
      static final int LOCAL = Utils.columnToIndex("AC");
    }

    static class Notes {
      static final int NB = Utils.columnToIndex("AD");
      static final int NOTE = Utils.columnToIndex("AE");
      static final int SEE_ALSO = Utils.columnToIndex("AF");
      static final int TECH_NOTE = Utils.columnToIndex("AG");
    }
  }

  public static List<NavigationLevel> parse(Workbook workbook) {
    List<NavigationLevel> navigationLevels = new ArrayList<>();

    // Expected to parse multiple sheets
    for (int sheetIdx: Arrays.asList(SheetIndices.UK_MILITARY_LIST)) {
      Deque<NavigationLevel> navLevelStack = new ArrayDeque<>();

      NavigationLevel rootNavigationLevel = new NavigationLevel("ROOT", "ROOT", -1);

      navLevelStack.push(rootNavigationLevel);

      Sheet sheet = workbook.getSheetAt(sheetIdx);
      for (int rowIdx = RowIndices.NAVIGATION_START; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) {
          continue;
        }

        for (int navColIdx = ColumnIndices.Navigation.START; navColIdx <= ColumnIndices.Navigation.END; navColIdx++) {
          String navCellValue = Utils.getCellStringValue(row, navColIdx);
          if (navCellValue == null) {
            continue;
          }

          String navCellAddress = row.getCell(navColIdx).getAddress().formatAsString();

          int currIndentLevel = navColIdx - ColumnIndices.Navigation.START;
          int prevIndentLevel = navLevelStack.peek().getLevel();

          NavigationLevel navigationLevel = new NavigationLevel(navCellAddress, navCellValue, currIndentLevel);

          try {
            NavigationExtras navigationExtras = getNavigationExtras(row);
            OnPageContent onPageContent = getOnPageContent(row);
            ControlListEntries controlListEntries = getControlListEntries(row);
            Buttons buttons = getButtons(row);
            Nesting nesting = getNesting(row);
            Loops loops = getLoops(row);
            Breadcrumbs breadcrumbs = getBreadcrumbs(row);
            Decontrols decontrols = getDecontrols(row);
            Definitions definitions = getDefinitions(row);
            Notes notes = getNotes(row);
            navigationLevel =
                new NavigationLevel(
                    navCellAddress,
                    navCellValue,
                    currIndentLevel,
                    navigationExtras,
                    onPageContent,
                    controlListEntries,
                    buttons,
                    nesting,
                    loops,
                    breadcrumbs,
                    decontrols,
                    definitions,
                    notes);
          } catch (ParserException e) {
            Logger.error("Error progressing nav cell: {}, {}", navCellAddress, e.getMessage());
          }

          if (currIndentLevel < prevIndentLevel) {
            // level higher: current = ML1a, previous = ML1a1
            while (navLevelStack.peek().getLevel() >= currIndentLevel) {
              navLevelStack.pop();
            }
            navLevelStack.peek().addSubNavigationLevel(navigationLevel);
            navLevelStack.push(navigationLevel);
          } else if (currIndentLevel > prevIndentLevel) {
            // level lower: current = ML1a1, previous = ML1
            navLevelStack.peek().addSubNavigationLevel(navigationLevel);
            navLevelStack.push(navigationLevel);
          } else {
            // level same: current = ML1b, previous = ML1a
            navLevelStack.pop();
            navLevelStack.peek().addSubNavigationLevel(navigationLevel);
            navLevelStack.push(navigationLevel);
          }

          break;
        }
      }
      navigationLevels.addAll(rootNavigationLevel.getSubNavigationLevels());
    }
    return navigationLevels;
  }

  private static NavigationExtras getNavigationExtras(Row row) {
    String divLineStr = Utils.getCellStringValue(row, ColumnIndices.NavigationExtras.DIV_LINE);
    boolean divLine = "X".equalsIgnoreCase(divLineStr);
    return  new NavigationExtras(divLine);
  }

  private static OnPageContent getOnPageContent(Row row) {
    String title = Utils.getCellStringValue(row, ColumnIndices.OnPageContent.TITLE);
    String explanatoryNotes = Utils.getCellStringValue(row, ColumnIndices.OnPageContent.EXPLANATORY_NOTES);
    return new OnPageContent(title, explanatoryNotes);
  }

  private static ControlListEntries getControlListEntries(Row row) {
    String rating = Utils.getCellStringValue(row, ColumnIndices.ControlListEntries.RATING);
    String priorityStr = Utils.getCellStringValue(row, ColumnIndices.ControlListEntries.PRIORITY);
    Integer priority = priorityStr == null ? null : Integer.parseInt(priorityStr);
    return new ControlListEntries(rating, priority);
  }

  private static Buttons getButtons(Row row) {
    String selectOne = Utils.getCellStringValue(row, ColumnIndices.Buttons.SELECT_ONE);
    String selectMany = Utils.getCellStringValue(row, ColumnIndices.Buttons.SELECT_MANY);
    if (StringUtils.isNoneBlank(selectOne, selectMany)) {
      throw new ParserException(String.format("Invalid button column state, select one: %s select many: %s", selectOne, selectMany));
    } else {
      if ("X".equalsIgnoreCase(selectOne)) {
        return Buttons.SELECT_ONE;
      } else if ("X".equalsIgnoreCase(selectMany)) {
        return Buttons.SELECT_MANY;
      } else {
        return null;
      }
    }
  }

  private static Nesting getNesting(Row row) {
    String anyOf = Utils.getCellStringValue(row, ColumnIndices.Nesting.ANY_OF);
    String allOf = Utils.getCellStringValue(row, ColumnIndices.Nesting.ALL_OF);
    if (StringUtils.isNoneBlank(anyOf, allOf)) {
      throw new ParserException(String.format("Invalid nesting column state, any of: %s all of: %s", anyOf, allOf));
    } else {
      if ("X".equalsIgnoreCase(anyOf)) {
        return Nesting.ANY_OF;
      } else if ("X".equalsIgnoreCase(allOf)) {
        return Nesting.ALL_OF;
      } else {
        return null;
      }
    }
  }

  private static Loops getLoops(Row row) {
    String relatedCodes = Utils.getCellStringValue(row, ColumnIndices.Loops.RELATED_CODES);
    String jumpTo = Utils.getCellStringValue(row, ColumnIndices.Loops.JUMP_TO);
    String defining = Utils.getCellStringValue(row, ColumnIndices.Loops.DEFINING);
    String deferred = Utils.getCellStringValue(row, ColumnIndices.Loops.DEFERRED);
    return new Loops(relatedCodes, jumpTo, defining, deferred);
  }

  private static Breadcrumbs getBreadcrumbs(Row row) {
    String breadcrumbText = Utils.getCellStringValue(row, ColumnIndices.Breadcrumbs.BREADCRUMB_TEXT);
    return new Breadcrumbs(breadcrumbText);
  }

  private static Decontrols getDecontrols(Row row) {
    String content = Utils.getCellStringValue(row, ColumnIndices.Decontrols.CONTENT);
    String note = Utils.getCellStringValue(row, ColumnIndices.Decontrols.NOTE);
    String title = Utils.getCellStringValue(row, ColumnIndices.Decontrols.TITLE);
    String explanatoryNotes = Utils.getCellStringValue(row, ColumnIndices.Decontrols.EXPLANATORY_NOTES);
    return new Decontrols(content, note, title, explanatoryNotes);
  }

  private static Definitions getDefinitions(Row row) {
    String local = Utils.getCellStringValue(row, ColumnIndices.Definitions.LOCAL);
    return new Definitions(local);
  }

  private static Notes getNotes(Row row) {
    String nb = Utils.getCellStringValue(row, ColumnIndices.Notes.NB);
    String note = Utils.getCellStringValue(row, ColumnIndices.Notes.NOTE);
    String seeAlso = Utils.getCellStringValue(row, ColumnIndices.Notes.SEE_ALSO);
    String techNote = Utils.getCellStringValue(row, ColumnIndices.Notes.TECH_NOTE);
    return new Notes(nb, note, seeAlso, techNote);
  }
}
