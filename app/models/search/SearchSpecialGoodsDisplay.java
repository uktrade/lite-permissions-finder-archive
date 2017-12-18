package models.search;

public class SearchSpecialGoodsDisplay {

  public final String pageTitle;
  public final String questionLabel;

  public SearchSpecialGoodsDisplay(Boolean isItem) {
    this.pageTitle = "Describe the item you're exporting";

    if(isItem) {
      this.questionLabel = "Was the item specifically designed or modifed for military use";
    } else {
      this.questionLabel = "Was this equipment, platform or system specifically designed or modifed for military use";
    }
  }

}
