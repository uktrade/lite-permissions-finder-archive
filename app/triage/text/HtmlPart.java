package triage.text;

import models.enums.HtmlType;

public class HtmlPart {

  private final HtmlType htmlType;
  private final String text;

  public HtmlPart(HtmlType htmlType, String text) {
    this.htmlType = htmlType;
    this.text = text;
  }

  public HtmlType getHtmlType() {
    return htmlType;
  }

  public String getText() {
    return text;
  }

}
