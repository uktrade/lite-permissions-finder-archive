package components.cms.parser.workbook;

import components.cms.parser.model.definition.Definition;
import components.cms.parser.util.Utils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DefinitionParser {

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  private static class SheetIndices {
    static final int DEFINITIONS = 1; // Sheet 1
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  private static class RowIndices {
    static final int START = 1; // Row 2
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  private static class ColumnIndices {
    static final int NAME = Utils.columnToIndex("A");
    static final int LIST = Utils.columnToIndex("B");
    static final int NEW_CONTENT = Utils.columnToIndex("D");
  }

  public static List<Definition> parse(Workbook workbook) {
    Sheet sheet = workbook.getSheetAt(SheetIndices.DEFINITIONS);

    // For each row in the sheet, map to a definition and then return as list
    return StreamSupport.stream(sheet.spliterator(), true)
            .skip(RowIndices.START) // Skip first row (header)
            .filter(Objects::nonNull) // Filter out null rows
            .map(row -> new Definition(
                    row.getRowNum(),
                    Utils.getCellValueAsString(row.getCell(ColumnIndices.NAME)),
                    Utils.getCellValueAsString(row.getCell(ColumnIndices.LIST)),
                    Utils.getCellValueAsString(row.getCell(ColumnIndices.NEW_CONTENT))))
         .collect(Collectors.toList());
  }
}
