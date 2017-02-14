package models.search;

import models.controlcode.ControlCodeSubJourney;
import play.data.Form;
import uk.gov.bis.lite.searchmanagement.api.view.SearchResultView;

import java.util.List;

public class SearchResultsDisplay {
  public final ControlCodeSubJourney controlCodeSubJourney;
  public final Form<?> form;
  public final String pageTitle;
  public final List<SearchResultView> results;
  public final int resultsDisplayCount;
  public final String lastChosenControlCode;
  public final String preResultsLabel;

  public SearchResultsDisplay(ControlCodeSubJourney controlCodeSubJourney, Form<?> form, List<SearchResultView> results, int resultsDisplayCount, String lastChosenControlCode) {
    this.controlCodeSubJourney = controlCodeSubJourney;
    this.form = form;
    this.results = results;
    this.resultsDisplayCount = resultsDisplayCount;
    this.lastChosenControlCode = lastChosenControlCode;
    String pageTitleWithMatches;
    String pageTitleWithoutMatches;
    if (controlCodeSubJourney == ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH) {
      this.preResultsLabel = "";
      pageTitleWithMatches = "Possible matches";
      pageTitleWithoutMatches = "Your search did not return any results";
    }
    else if (controlCodeSubJourney == ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.preResultsLabel = "Select the closest match to the physical item your software is used with";
      pageTitleWithMatches = "Item related to your software";
      pageTitleWithoutMatches = "Your search did not return any results";
    }
    else if (controlCodeSubJourney == ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.preResultsLabel = "Select the closest match to the physical item your technology is used with";
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

  public SearchResultsDisplay(ControlCodeSubJourney controlCodeSubJourney, Form<?> form, List<SearchResultView> results, int resultsDisplayCount) {
    this(controlCodeSubJourney, form, results, resultsDisplayCount, null);
  }
}
