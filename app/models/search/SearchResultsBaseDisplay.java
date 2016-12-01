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
  public final GoodsType goodsType;
  public final List<Result> results;
  public final int resultsDisplayCount;
  public final String lastChosenControlCode;

  public SearchResultsBaseDisplay(ControlCodeJourney controlCodeJourney, Form<?> form, GoodsType goodsType, List<Result> results, int resultsDisplayCount, String lastChosenControlCode) {
    this.controlCodeJourney = controlCodeJourney;
    this.form = form;
    this.goodsType = goodsType;
    this.results = results;
    this.resultsDisplayCount = resultsDisplayCount;
    this.lastChosenControlCode = lastChosenControlCode;
    if (controlCodeJourney== ControlCodeJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.PhysicalGoodsSearchResultsController.handleSearchSubmit().url();
    }
    else if (controlCodeJourney== ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.PhysicalGoodsSearchResultsController.renderSearchRelatedToSoftwareForm().url();
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

  public SearchResultsBaseDisplay(ControlCodeJourney controlCodeJourney, Form<?> form, GoodsType goodsType, List<Result> results, int resultsDisplayCount) {
    this(controlCodeJourney, form, goodsType, results, resultsDisplayCount, null);
  }
}
