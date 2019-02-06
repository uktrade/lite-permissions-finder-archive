package components.cms.parser.util;

import exceptions.ServiceException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.slf4j.LoggerFactory;

public class Utils {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Utils.class);

  public static String getCellValueAsString(Cell cell) {
    String cellValue;

    if (cell == null) {
      return null;
    }

    switch (cell.getCellType()) {
      case STRING:
        cellValue = cell.getStringCellValue();
        break;
      case NUMERIC:
        cellValue = Double.toString(cell.getNumericCellValue());
        break;
      case BOOLEAN:
        cellValue = Boolean.toString(cell.getBooleanCellValue());
        break;
      default:
        return null;
    }

    return (StringUtils.isNotBlank(cellValue)) ? cellValue : null;
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

  public static boolean isCellMarkedWithX(Cell cell) {
    String cellContents = getCellValueAsString(cell);
    return "X".equalsIgnoreCase(cellContents);
  }

  public static List<String> getCellValuesAsListOfStrings(Cell cell, String separator) {
    return splitStringIntoList(Optional.ofNullable(getCellValueAsString(cell)).orElse(""), separator);
  }

  public static List<String> splitStringIntoList(String str, String separator) {
    return Arrays.stream(str.trim().split(separator))
      .map(String::trim)
      .filter(StringUtils::isNotEmpty)
      .collect(Collectors.toList());
  }
}
