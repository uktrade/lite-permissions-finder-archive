package components.cms.parser.workbook;

import components.cms.parser.util.Utils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import models.cms.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InformationParser {

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  private static class SheetIndices {
    static final int INFORMATION = 0; // Sheet 1
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  private static class RowIndices {
    static final int START = 4; // Row 5
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  private static class ColumnIndices {
    static final int C = Utils.columnToIndex("C");
  }

  public static SpreadsheetVersion parse(Workbook workbook) {
    Sheet sheet = workbook.getSheetAt(SheetIndices.INFORMATION);

    return new SpreadsheetVersion(Utils.getCellValueAsString(sheet.getRow(RowIndices.START).getCell(ColumnIndices.C)));
  }

}
