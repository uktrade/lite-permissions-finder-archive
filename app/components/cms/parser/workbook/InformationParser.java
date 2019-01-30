package components.cms.parser.workbook;

import components.cms.parser.util.Utils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import models.cms.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

  public static SpreadsheetVersion parse(Workbook workbook, File file, String filename) throws IOException, NoSuchAlgorithmException {
    Sheet sheet = workbook.getSheetAt(SheetIndices.INFORMATION);
    String sha1 = createSHA1(file);
    return new SpreadsheetVersion(filename,
      Utils.getCellValueAsString(sheet.getRow(RowIndices.START).getCell(ColumnIndices.C)),
      sha1);
  }

  private static String createSHA1(File file) throws IOException, NoSuchAlgorithmException {
    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
    try (InputStream input = new FileInputStream(file)) {

      byte[] buffer = new byte[8192];
      int len = input.read(buffer);

      while (len != -1) {
        sha1.update(buffer, 0, len);
        len = input.read(buffer);
      }

      return new HexBinaryAdapter().marshal(sha1.digest());
    }
  }

}
