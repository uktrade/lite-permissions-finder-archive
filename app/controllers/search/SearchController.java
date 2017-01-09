package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.controlcode.ControlCodeSubJourney;
import models.search.SearchDisplay;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.mvc.Result;
import views.html.search.search;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class SearchController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public SearchController(JourneyManager journeyManager,
                          FormFactory formFactory,
                          PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  private CompletionStage<Result> renderForm(ControlCodeSubJourney controlCodeSubJourney) {
    Optional<SearchForm> templateFormOptional = permissionsFinderDao.getPhysicalGoodsSearchForm(controlCodeSubJourney);
    SearchForm templateForm = templateFormOptional.isPresent() ? templateFormOptional.get() : new SearchForm();
    return completedFuture(ok(search.render(
        new SearchDisplay(controlCodeSubJourney, formFactory.form(SearchForm.class).fill(templateForm))
    )));
  }

  public CompletionStage<Result> renderSearchForm() {
    return renderForm(models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> renderSearchRelatedToForm (String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::renderForm);
  }

  private CompletionStage<Result> handleSubmit(ControlCodeSubJourney controlCodeSubJourney) {
    Form<SearchForm> form = bindSearchForm();

    if(form.hasErrors()) {
      return completedFuture(ok(search.render(new SearchDisplay(controlCodeSubJourney, form))));
    }
    permissionsFinderDao.savePhysicalGoodSearchForm(controlCodeSubJourney, form.get());
    permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(controlCodeSubJourney,
        SearchResultsController.PAGINATION_SIZE);
    permissionsFinderDao.clearPhysicalGoodSearchLastChosenControlCode(controlCodeSubJourney);
    return journeyManager.performTransition(Events.SEARCH_PHYSICAL_GOODS);
  }

  public CompletionStage<Result> handleSearchSubmit() {
    return handleSubmit(models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> handleSearchRelatedToSubmit (String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::handleSubmit);
  }

  public Form<SearchForm> bindSearchForm() {
    // Trims whitespace from the form fields
    Map<String, String> map = formFactory.form(SearchForm.class).bindFromRequest().data().entrySet()
        .stream()
        .map(entry -> {
          if (entry.getValue() != null) {
            entry.setValue(entry.getValue().trim());
          }
          return entry;
        })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return formFactory.form(SearchForm.class).bind(map);
  }

  public static String getSearchTerms(SearchForm form) {
    return Arrays.asList(form.description, form.component).stream()
        .filter(StringUtils::isNoneBlank)
        .collect(Collectors.joining(", "));
  }

  public static class SearchForm {

    @Constraints.Required(message = "You must enter a description of your goods")
    public String description;

    public String component;

  }

}