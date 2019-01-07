package components.cms.parser;

import components.cms.parser.model.NavigationLevel;
import components.cms.parser.model.definition.Definition;
import components.cms.parser.workbook.DefinitionParser;
import components.cms.parser.workbook.NavigationParser;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class Parser {
  public ParserResult parse(File file) throws IOException {
    try (FileInputStream excelFile = new FileInputStream(file)) {
      Workbook workbook = new XSSFWorkbook(excelFile);

      List<NavigationLevel> navigationLevels = NavigationParser.parse(workbook);
      List<Definition> definitions = DefinitionParser.parse(workbook);

      return new ParserResult(navigationLevels, definitions);
    }
  }
}
