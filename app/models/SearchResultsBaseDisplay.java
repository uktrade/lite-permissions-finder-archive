package models;

import components.services.search.Result;
import controllers.search.routes;
import play.data.Form;

import java.util.List;

public class SearchResultsBaseDisplay {
  public final Form<?> form;
  public final String formAction;
  public final GoodsType goodsType;
  public final List<Result> results;
  public final int resultsDisplayCount;
  public final String lastChosenControlCode;

  public SearchResultsBaseDisplay(Form<?> form, GoodsType goodsType, List<Result> results, int resultsDisplayCount, String lastChosenControlCode) {
    this.form = form;
    this.formAction = routes.PhysicalGoodsSearchResultsController.handleSubmit().url();
    this.goodsType = goodsType;
    this.results = results;
    this.resultsDisplayCount = resultsDisplayCount;
    this.lastChosenControlCode = lastChosenControlCode;
  }

  public SearchResultsBaseDisplay(Form<?> form, GoodsType goodsType, List<Result> results, int resultsDisplayCount) {
    this(form, goodsType, results, resultsDisplayCount, null);
  }
}
