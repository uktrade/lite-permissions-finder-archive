package components.cms.parser.workbook;

import components.cms.parser.ParserException;
import components.cms.parser.model.NavigationLevel;
import components.cms.parser.model.navigation.column.Breadcrumbs;
import components.cms.parser.model.navigation.column.Buttons;
import components.cms.parser.model.navigation.column.ControlListEntries;
import components.cms.parser.model.navigation.column.Decontrols;
import components.cms.parser.model.navigation.column.Definitions;
import components.cms.parser.model.navigation.column.Loops;
import components.cms.parser.model.navigation.column.Nesting;
import components.cms.parser.model.navigation.column.Notes;
import components.cms.parser.model.navigation.column.OnPageContent;
import components.cms.parser.model.navigation.column.Redirect;
import components.cms.parser.util.Utils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.LoggerFactory;

import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NavigationParser {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NavigationParser.class);

    public static final Map<Integer, String> sheetIndices;
    static
    {
        sheetIndices = new HashMap<>();
        sheetIndices.put(2, "UK_MILITARY_LIST"); // Column 2
        sheetIndices.put(3, "DUAL_USE_LIST"); // Column 3
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class RowIndices {
        static final int NAVIGATION_START = 3; // Row 4
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class ColumnIndices {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static class Navigation {
            static final int START = Utils.columnToIndex("A");
            static final int END = Utils.columnToIndex("J");
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static class OnPageContent {
            static final int TITLE = Utils.columnToIndex("K");
            static final int EXPLANATORY_NOTES = Utils.columnToIndex("L");
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static class ControlListEntries {
            static final int RATING = Utils.columnToIndex("M");
            static final int PRIORITY = Utils.columnToIndex("N");
            static final int DECONTROL = Utils.columnToIndex("AI");
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static class Buttons {
            static final int SELECT_ONE = Utils.columnToIndex("O");
            static final int SELECT_MANY = Utils.columnToIndex("P");
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static class Nesting {
            static final int ANY_OF = Utils.columnToIndex("Q");
            static final int ALL_OF = Utils.columnToIndex("R");
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static class Loops {
            static final int RELATED_CODES = Utils.columnToIndex("S");
            static final int JUMP_TO = Utils.columnToIndex("T");
            static final int DEFINING = Utils.columnToIndex("U");
            static final int DEFERRED = Utils.columnToIndex("V");
            static final int JUMP_TO_CONTROL_CODES = Utils.columnToIndex("AH");
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static class Breadcrumbs {
            static final int BREADCRUMB_TEXT = Utils.columnToIndex("W");
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static class Decontrols {
            static final int CONTENT = Utils.columnToIndex("X");
            static final int NOTE = Utils.columnToIndex("Y");
            static final int TITLE = Utils.columnToIndex("Z");
            static final int EXPLANATORY_NOTES = Utils.columnToIndex("AA");
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static class Definitions {
            static final int LOCAL = Utils.columnToIndex("AB");
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static class Notes {
            static final int NB = Utils.columnToIndex("AC");
            static final int NOTE = Utils.columnToIndex("AD");
            static final int SEE_ALSO = Utils.columnToIndex("AE");
            static final int TECHNICAL_NOTE = Utils.columnToIndex("AF");
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static class Redirect {
            static final int TCFCF = Utils.columnToIndex("AG"); // Too complicated for Codefinder
        }
    }

    public static List<NavigationLevel> parse(Workbook workbook) {
        List<NavigationLevel> navigationLevels = new ArrayList<>();

        // Loop through sheets
        for (Map.Entry<Integer, String> entry : sheetIndices.entrySet()) {
            Deque<NavigationLevel> navLevelStack = new ArrayDeque<>();

            // Create a base NavigationLevel that can hold all parsed in
            NavigationLevel rootNavigationLevel = new NavigationLevel("ROOT", "ROOT", -1, entry.getValue());

            navLevelStack.push(rootNavigationLevel);

            Sheet sheet = workbook.getSheetAt(entry.getKey());

            for (int rowIndex = RowIndices.NAVIGATION_START; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                for (int navColIndex = ColumnIndices.Navigation.START; navColIndex <= ColumnIndices.Navigation.END; navColIndex++) {
                    String navCellValue = Utils.getCellValueAsString(row.getCell(navColIndex));
                    if (navCellValue == null) {
                        continue;
                    }

                    String navCellAddress = row.getCell(navColIndex).getAddress().formatAsString();

                    int currIndentLevel = navColIndex - ColumnIndices.Navigation.START;
                    int prevIndentLevel = navLevelStack.peek().getLevel();

                    NavigationLevel navigationLevel = new NavigationLevel(navCellAddress, navCellValue, currIndentLevel, entry.getValue());

                    try {
                        OnPageContent onPageContent = getOnPageContent(row);
                        ControlListEntries controlListEntries = getControlListEntries(row);
                        Buttons buttons = getButtons(row);
                        Nesting nesting = getNesting(row);
                        Loops loops = getLoops(row);
                        Breadcrumbs breadcrumbs = getBreadcrumbs(row);
                        Decontrols decontrols = getDecontrols(row);
                        Definitions definitions = getDefinitions(row);
                        Notes notes = getNotes(row);
                        Redirect redirect = getRedirect(row);
                        navigationLevel =
                                new NavigationLevel(
                                        navCellAddress,
                                        navCellValue,
                                        currIndentLevel,
                                        entry.getValue(),
                                        onPageContent,
                                        controlListEntries,
                                        buttons,
                                        nesting,
                                        loops,
                                        breadcrumbs,
                                        decontrols,
                                        definitions,
                                        notes,
                                        redirect);
                    } catch (ParserException e) {
                        LOGGER.error("Error progressing nav cell: {}, {}", navCellAddress, e.getMessage());
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

            navigationLevels.add(rootNavigationLevel);
        }

        return navigationLevels;
    }

    private static OnPageContent getOnPageContent(Row row) {
        String title = Utils.getCellValueAsString(row.getCell(ColumnIndices.OnPageContent.TITLE));
        String explanatoryNotes = Utils.getCellValueAsString(row.getCell(ColumnIndices.OnPageContent.EXPLANATORY_NOTES));
        return new OnPageContent(title, explanatoryNotes);
    }

    private static ControlListEntries getControlListEntries(Row row) {
        String rating = Utils.getCellValueAsString(row.getCell(ColumnIndices.ControlListEntries.RATING));
        String priorityStr = Utils.getCellValueAsString(row.getCell(ColumnIndices.ControlListEntries.PRIORITY));
        Integer priority = priorityStr == null ? null : (int)Double.parseDouble(priorityStr);
        String decontrolStr = Utils.getCellValueAsString(row.getCell(ColumnIndices.ControlListEntries.DECONTROL));
        Boolean decontrolled = "X".equalsIgnoreCase(decontrolStr);

        return new ControlListEntries(rating, priority, decontrolled);
    }

    private static Buttons getButtons(Row row) {
        String selectOne = Utils.getCellValueAsString(row.getCell(ColumnIndices.Buttons.SELECT_ONE));
        String selectMany = Utils.getCellValueAsString(row.getCell(ColumnIndices.Buttons.SELECT_MANY));
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
        String anyOf = Utils.getCellValueAsString(row.getCell(ColumnIndices.Nesting.ANY_OF));
        String allOf = Utils.getCellValueAsString(row.getCell(ColumnIndices.Nesting.ALL_OF));
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
        String relatedCodes = Utils.getCellValueAsString(row.getCell(ColumnIndices.Loops.RELATED_CODES));
        String jumpTo = Utils.getCellValueAsString(row.getCell(ColumnIndices.Loops.JUMP_TO));
        String defining = Utils.getCellValueAsString(row.getCell(ColumnIndices.Loops.DEFINING));
        String deferred = Utils.getCellValueAsString(row.getCell(ColumnIndices.Loops.DEFERRED));
        List<String> jumpToControlCodes = Utils.getCellValuesAsListOfStrings(row.getCell(ColumnIndices.Loops.JUMP_TO_CONTROL_CODES), ",");
        return new Loops(relatedCodes, jumpTo, defining, deferred, jumpToControlCodes);
    }

    private static Breadcrumbs getBreadcrumbs(Row row) {
        String breadcrumbText = Utils.getCellValueAsString(row.getCell(ColumnIndices.Breadcrumbs.BREADCRUMB_TEXT));
        return new Breadcrumbs(breadcrumbText);
    }

    private static Decontrols getDecontrols(Row row) {
        String content = Utils.getCellValueAsString(row.getCell(ColumnIndices.Decontrols.CONTENT));
        String note = Utils.getCellValueAsString(row.getCell(ColumnIndices.Decontrols.NOTE));
        String title = Utils.getCellValueAsString(row.getCell(ColumnIndices.Decontrols.TITLE));
        String explanatoryNotes = Utils.getCellValueAsString(row.getCell(ColumnIndices.Decontrols.EXPLANATORY_NOTES));
        return new Decontrols(content, note, title, explanatoryNotes);
    }

    private static Definitions getDefinitions(Row row) {
        String local = Utils.getCellValueAsString(row.getCell(ColumnIndices.Definitions.LOCAL));
        return new Definitions(local);
    }

    private static Notes getNotes(Row row) {
        String nb = Utils.getCellValueAsString(row.getCell(ColumnIndices.Notes.NB));
        String note = Utils.getCellValueAsString(row.getCell(ColumnIndices.Notes.NOTE));
        String seeAlso = Utils.getCellValueAsString(row.getCell(ColumnIndices.Notes.SEE_ALSO));
        String techNote = Utils.getCellValueAsString(row.getCell(ColumnIndices.Notes.TECHNICAL_NOTE));
        return new Notes(nb, note, seeAlso, techNote);
    }

    private static Redirect getRedirect(Row row) {
      return Utils.isCellMarkedWithX(row.getCell(ColumnIndices.Redirect.TCFCF)) ? Redirect.TOO_COMPLEX_FOR_CODE_FINDER
        : Redirect.NONE;
    }
}
