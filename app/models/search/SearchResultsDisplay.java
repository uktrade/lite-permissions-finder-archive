package models.search;

import components.services.search.Result;
import controllers.search.routes;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import play.data.Form;

import java.util.List;

public class SearchResultsDisplay {
  public final ControlCodeSubJourney controlCodeSubJourney;
  public final Form<?> form;
  public final String formAction;
  public final String pageTitle;
  public final List<Result> results;
  public final int resultsDisplayCount;
  public final String lastChosenControlCode;
  public final String preResultsLabel;

  public SearchResultsDisplay(ControlCodeSubJourney controlCodeSubJourney, Form<?> form, List<Result> results, int resultsDisplayCount, String lastChosenControlCode) {
    this.controlCodeSubJourney = controlCodeSubJourney;
    this.form = form;
    this.results = results;
    this.resultsDisplayCount = resultsDisplayCount;
    this.lastChosenControlCode = lastChosenControlCode;
    String pageTitleWithMatches;
    String pageTitleWithoutMatches;
    if (controlCodeSubJourney == ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.SearchResultsController.handleSearchSubmit().url();
      this.preResultsLabel = "";
      pageTitleWithMatches = "Possible matches";
      pageTitleWithoutMatches = "Your search did not return any results";
    }
    else if (controlCodeSubJourney == ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.SearchResultsController.handleSearchRelatedToSubmit(GoodsType.SOFTWARE.urlString()).url();
      this.preResultsLabel = "Select the closest match to the item your software is used with";
      pageTitleWithMatches = "Item related to your software";
      pageTitleWithoutMatches = "Your search did not return any results";
    }
    else if (controlCodeSubJourney == ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.formAction = routes.SearchResultsController.handleSearchRelatedToSubmit(GoodsType.TECHNOLOGY.urlString()).url();
      this.preResultsLabel = "Select the closest match to the item your technology is used with";
      pageTitleWithMatches = "Item related to your technology";
      pageTitleWithoutMatches = "Your search did not return any results";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }

    if (results.size() > 0) {
      this.pageTitle = pageTitleWithMatches;
    }
    else {
      this.pageTitle = pageTitleWithoutMatches;
    }
  }

  public SearchResultsDisplay(ControlCodeSubJourney controlCodeSubJourney, Form<?> form, List<Result> results, int resultsDisplayCount) {
    this(controlCodeSubJourney, form, results, resultsDisplayCount, null);
  }
}
