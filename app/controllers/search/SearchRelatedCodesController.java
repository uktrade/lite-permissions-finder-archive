package controllers.search;

import static play.mvc.Results.ok;

import com.google.common.base.Enums;
import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.search.relatedcodes.RelatedCodesServiceClient;
import components.services.search.relatedcodes.RelatedCodesServiceResult;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.controlcode.BackType;
import models.controlcode.ControlCodeSubJourney;
import models.controlcode.ControlCodeVariant;
import models.search.SearchRelatedCodesDisplay;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.search.searchRelatedCodes;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class SearchRelatedCodesController {

  public static final int PAGINATION_SIZE = 5;

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final RelatedCodesServiceClient relatedCodesServiceClient;
  private final HttpExecutionContext httpExecutionContext;
  private final PermissionsFinderDao permissionsFinderDao;

  public enum SearchRelatedCodesAction{
    NONE_MATCHED,
    SHOW_MORE,
    PICK_FROM_RESULTS_AGAIN;

    public static Optional<SearchRelatedCodesAction> getMatched(String name) {
      if (StringUtils.isEmpty(name)) {
        return Optional.empty();
      }
      else {
        return Enums.getIfPresent(SearchRelatedCodesAction.class, name)
            .transform(java.util.Optional::of)
            .or(java.util.Optional.empty());
      }
    }
  }

  @Inject
  public SearchRelatedCodesController(JourneyManager journeyManager,
                                      FormFactory formFactory,
                                      RelatedCodesServiceClient relatedCodesServiceClient,
                                      HttpExecutionContext httpExecutionContext,
                                      PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.relatedCodesServiceClient = relatedCodesServiceClient;
    this.httpExecutionContext = httpExecutionContext;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public CompletionStage<Result> renderForm(String goodsTypeText) {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveUrlToSubJourneyAndUpdateContext(ControlCodeVariant.SEARCH.urlString(), goodsTypeText);
    return renderFormInternal(controlCodeSubJourney);
  }

  private CompletionStage<Result> renderFormInternal(ControlCodeSubJourney controlCodeSubJourney) {
    String resultsControlCode = permissionsFinderDao.getSearchResultsLastChosenControlCode(controlCodeSubJourney);
    return relatedCodes(resultsControlCode)
        .thenApplyAsync(result -> {
          int displayCount = Math.min(result.relatedCodes.size(), PAGINATION_SIZE);
          Optional<Integer> optionalDisplayCount = permissionsFinderDao.getSearchRelatedCodesPaginationDisplayCount(controlCodeSubJourney);
          if (optionalDisplayCount.isPresent()) {
            displayCount = Math.min(result.relatedCodes.size(), optionalDisplayCount.get());
          }
          else {
            permissionsFinderDao.saveSearchRelatedCodesPaginationDisplayCount(controlCodeSubJourney, displayCount);
          }

          // TODO, remove to enable "Show more results" functionality
          displayCount = result.relatedCodes.size();

          String lastChosenControlCode = permissionsFinderDao.getSearchRelatedCodesLastChosenControlCode(controlCodeSubJourney);
          SearchRelatedCodesDisplay display = new SearchRelatedCodesDisplay(controlCodeSubJourney, result.groupTitle, result.relatedCodes, displayCount, lastChosenControlCode);
          return ok(searchRelatedCodes.render(formFactory.form(SearchRelatedCodesForm.class), display));
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> handleSubmit() {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveContextToSubJourney();
    return handleSubmitInternal(controlCodeSubJourney);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Form<SearchRelatedCodesForm> form = formFactory.form(SearchRelatedCodesForm.class).bindFromRequest();

    if (form.hasErrors()) {
      String resultsControlCode = permissionsFinderDao.getSearchResultsLastChosenControlCode(controlCodeSubJourney);
      return relatedCodes(resultsControlCode)
          .thenApplyAsync(result -> {
            int displayCount = Integer.parseInt(form.field("relatedCodesDisplayCount").value());
            int newDisplayCount = Math.min(displayCount, result.relatedCodes.size());
            if (displayCount != newDisplayCount) {
              permissionsFinderDao.saveSearchRelatedCodesPaginationDisplayCount(controlCodeSubJourney, newDisplayCount);
            }
            SearchRelatedCodesDisplay display = new SearchRelatedCodesDisplay(controlCodeSubJourney, result.groupTitle, result.relatedCodes, newDisplayCount);
            return ok(searchRelatedCodes.render(form, display));
          }, httpExecutionContext.current());
    }

    Optional<SearchRelatedCodesAction> action = SearchRelatedCodesAction.getMatched(form.get().action);
    if (action.isPresent()){
      switch (action.get()) {
        case NONE_MATCHED:
          return journeyManager.performTransition(Events.NONE_MATCHED);
        case SHOW_MORE:
          String resultsControlCode = permissionsFinderDao.getSearchResultsLastChosenControlCode(controlCodeSubJourney);
          return relatedCodes(resultsControlCode)
              .thenApplyAsync(result -> {
                int displayCount = Integer.parseInt(form.get().relatedCodesDisplayCount);
                int newDisplayCount = Math.min(displayCount + PAGINATION_SIZE, result.relatedCodes.size());
                if (displayCount != newDisplayCount) {
                  permissionsFinderDao.saveSearchRelatedCodesPaginationDisplayCount(controlCodeSubJourney, newDisplayCount);
                }
                SearchRelatedCodesDisplay display = new SearchRelatedCodesDisplay(controlCodeSubJourney, result.groupTitle, result.relatedCodes, newDisplayCount);
                return ok(searchRelatedCodes.render(form, display));
              }, httpExecutionContext.current());
        case PICK_FROM_RESULTS_AGAIN:
          return journeyManager.performTransition(Events.BACK, BackType.RESULTS);
      }
    }

    String relatedCode = form.get().relatedCode;
    if (StringUtils.isNotEmpty(relatedCode)) {
      int displayCount = Integer.parseInt(form.get().relatedCodesDisplayCount);
      permissionsFinderDao.clearAndUpdateControlCodeSubJourneyDaoFieldsIfChanged(controlCodeSubJourney, relatedCode);
      permissionsFinderDao.saveSearchRelatedCodesPaginationDisplayCount(controlCodeSubJourney, displayCount);
      permissionsFinderDao.saveSearchRelatedCodesLastChosenControlCode(controlCodeSubJourney, relatedCode);
      return journeyManager.performTransition(Events.CONTROL_CODE_SELECTED);
    }

    throw new FormStateException("Unhandled form state");
  }

  public CompletionStage<RelatedCodesServiceResult> relatedCodes(String resultsControlCode) {
    return relatedCodesServiceClient.get(resultsControlCode);
  }


  public static class SearchRelatedCodesForm {

    public String relatedCode;

    public String action;

    public String controlCodeSubJourney;

    public String relatedCodesDisplayCount;

    public String paginationSize;
  }
}
