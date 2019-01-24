package components.cms.parser.model.definition;

public class Definition {
  private final int rowNumber;
  private final String name;
  private final String list;
  private final String newContent;

  public Definition(int rowNumber, String name, String list, String newContent) {
    this.rowNumber = rowNumber;
    this.name = name;
    this.list = list;
    this.newContent = newContent;
  }

  public int getRowNumber() {
    return rowNumber;
  }

  public String getName() {
    return name;
  }

  public String getList() {
    return list;
  }

  public String getNewContent() {
    return newContent;
  }
}
