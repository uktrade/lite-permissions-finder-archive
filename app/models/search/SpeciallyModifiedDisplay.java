package models.search;

public class SpeciallyModifiedDisplay {

  public final String pageTitle;
  public final String componentQuestion;
  public final String completeItemQuestion;
  public final String questionLabel;

  public SpeciallyModifiedDisplay(Boolean isComponent) {
    this.pageTitle = "Describe the item you're exporting";
    this.completeItemQuestion = "Was the item specifically designed or modifed for military use";
    this.componentQuestion = "Was this equipment, platform or system specifically designed or modifed for military use";

    if(isComponent) {
      this.questionLabel = componentQuestion;
    } else {
      this.questionLabel = completeItemQuestion;
    }
  }

}
