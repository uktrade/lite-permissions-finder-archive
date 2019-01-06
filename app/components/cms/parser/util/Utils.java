package components.cms.parser.util;

import exceptions.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.LoggerFactory;

public class Utils {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Utils.class);

  public static String getCellValue(Cell cell) {
    String cellValue;

    if (cell == null) {
      return null;
    } else if (cell.getCellTypeEnum() == CellType.STRING) {
      cellValue = cell.getStringCellValue();
    } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
      cellValue = Integer.toString((int) cell.getNumericCellValue());
    } else if (cell.getCellTypeEnum() == CellType.BOOLEAN) {
      cellValue = Boolean.toString(cell.getBooleanCellValue());
    } else {
      return null;
    }

    if (StringUtils.isNotBlank(cellValue)) {
      return cellValue;
    } else {
      return null;
    }
  }

  public static String getCellValueByRowAndIndex(Row row, int index) {
    Cell cell = row.getCell(index);
    return getCellValue(cell);
  }

  public static int columnToIndex(String column) {
    if (column == null || column.length() > 2 || column.length() == 0) {
      throw new ServiceException(String.format("Column %s is invalid", column));
    }
    int index;
    String upperColumn = column.toUpperCase();
    if (upperColumn.length() == 1) {
      index = upperColumn.charAt(0) - 65;
    } else {
      index = (((upperColumn.charAt(0) - 65) + 1) * 26) + (upperColumn.charAt(1) - 65);
    }
    LOGGER.info("Column {} produced index {}", column, index);
    return index;
  }
}
