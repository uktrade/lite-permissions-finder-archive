package models.search;

import components.services.search.Result;
import controllers.search.routes;
import models.GoodsType;
import models.controlcode.ControlCodeJourney;
import play.data.Form;

import java.util.List;

public class SearchResultsBaseDisplay {
  public final ControlCodeJourney controlCodeJourney;
  public final Form<?> form;
  public final String formAction;
  public final String pageTitle;
  public final GoodsType goodsType;
  public final List<Result> results;
  public final int resultsDisplayCount;
  public final String lastChosenControlCode;
  public final String preResultsLabel;

  public SearchResultsBaseDisplay(ControlCodeJourney controlCodeJourney, Form<?> form, GoodsType goodsType, List<Result> results, int resultsDisplayCount, String lastChosenControlCode) {
    this.controlCodeJourney = controlCodeJourney;
    this.form = form;
    this.goodsType = goodsType;
    this.results = results;
    this.resultsDisplayCount = resultsDisplayCount;
    this.lastChosenControlCode = lastChosenControlCode;
    String pageTitleWithMatches;
    String pageTitleWithoutMatches;
    if (controlCodeJourney== ControlCodeJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.PhysicalGoodsSearchResultsController.handleSearchSubmit().url();
      this.preResultsLabel = "";
      pageTitleWithMatches = "Possible matches";
      pageTitleWithoutMatches = "Your search did not return any results";
    }
    else if (controlCodeJourney== ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.PhysicalGoodsSearchResultsController.handleSearchRelatedToSubmit(GoodsType.SOFTWARE.urlString()).url();
      this.preResultsLabel = "Select the closest match to the item your software is used with";
      pageTitleWithMatches = "Item related to your software";
      pageTitleWithoutMatches = "Your search did not return any results";
    }
    else if (controlCodeJourney== ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.formAction = routes.PhysicalGoodsSearchResultsController.handleSearchRelatedToSubmit(GoodsType.TECHNOLOGY.urlString()).url();
      this.preResultsLabel = "Select the closest match to the item your technology is used with";
      pageTitleWithMatches = "Item related to your technology";
      pageTitleWithoutMatches = "Your search did not return any results";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }

    if (results.size() > 0) {
      this.pageTitle = pageTitleWithMatches;
    }
    else {
      this.pageTitle = pageTitleWithoutMatches;
    }
  }

  public SearchResultsBaseDisplay(ControlCodeJourney controlCodeJourney, Form<?> form, GoodsType goodsType, List<Result> results, int resultsDisplayCount) {
    this(controlCodeJourney, form, goodsType, results, resultsDisplayCount, null);
  }
}
