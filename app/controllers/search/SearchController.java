package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.controlcode.ControlCodeSubJourney;
import models.controlcode.ControlCodeVariant;
import models.search.SearchDisplay;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
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

  public CompletionStage<Result> renderForm(String goodsTypeText) {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveUrlToSubJourneyAndUpdateContext(ControlCodeVariant.SEARCH.urlString(), goodsTypeText);
    return renderFormInternal(controlCodeSubJourney);
  }

  private CompletionStage<Result> renderFormInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Optional<SearchForm> templateFormOptional = permissionsFinderDao.getPhysicalGoodsSearchForm(controlCodeSubJourney);
    SearchForm templateForm = templateFormOptional.isPresent() ? templateFormOptional.get() : new SearchForm();
    return completedFuture(ok(search.render(formFactory.form(SearchForm.class).fill(templateForm),
        new SearchDisplay(controlCodeSubJourney)
    )));
  }

  public CompletionStage<Result> handleSubmit() {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveContextToSubJourney();
    return handleSubmitInternal(controlCodeSubJourney);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Form<SearchForm> form = bindSearchForm();

    if(form.hasErrors()) {
      return completedFuture(ok(search.render(form, new SearchDisplay(controlCodeSubJourney))));
    }
    permissionsFinderDao.savePhysicalGoodSearchForm(controlCodeSubJourney, form.get());
    permissionsFinderDao.saveSearchResultsPaginationDisplayCount(controlCodeSubJourney,
        SearchResultsController.PAGINATION_SIZE);
    permissionsFinderDao.clearSearchResultsLastChosenControlCode(controlCodeSubJourney);
    return journeyManager.performTransition(Events.SEARCH_PHYSICAL_GOODS);
  }

  private Form<SearchForm> bindSearchForm() {
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
    if (form.isComponent) {
     return Arrays.asList(form.description, form.component).stream()
          .filter(StringUtils::isNoneBlank)
          .collect(Collectors.joining(", "));
    }
    else {
      return form.description;
    }
  }

  public static class SearchForm {

    @Required(message = "You must answer this question")
    public Boolean isComponent;

    @Required(message = "You must enter a description of your goods")
    public String description;

    public String component;

  }

}