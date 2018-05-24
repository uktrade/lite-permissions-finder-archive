package components.cms.parser.workbook;

import components.cms.parser.model.definition.Definition;
import components.cms.parser.util.Utils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.List;

public class DefinitionParser {
  private static class SheetIndices {
    static final int DEFINITIONS = 3; // Sheet 4
  }

  private static class RowIndices {
    static final int START = 1; // 2
  }

  private static class ColumnIndices {
    static final int NAME = Utils.columnToIndex("A");
    static final int LIST = Utils.columnToIndex("B");
    static final int NEW_CONTENT = Utils.columnToIndex("D");
  }

  public static List<Definition> parse(Workbook workbook) {
    Sheet sheet = workbook.getSheetAt(SheetIndices.DEFINITIONS);
    List<Definition> definitions = new ArrayList<>();
    for (int rowIdx = RowIndices.START; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
      Row row = sheet.getRow(rowIdx);
      if (row == null) {
        continue;
      }

      String name = Utils.getCellStringValue(row, ColumnIndices.NAME);
      String list = Utils.getCellStringValue(row, ColumnIndices.LIST);
      String newContent = Utils.getCellStringValue(row, ColumnIndices.NEW_CONTENT);
      definitions.add(new Definition(Integer.toString(row.getRowNum()), name, list, newContent));
    }
    return definitions;
  }
}
