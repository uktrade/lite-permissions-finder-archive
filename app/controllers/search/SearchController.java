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
import play.data.validation.Constraints.MaxLength;
import play.mvc.Result;
import views.html.search.search;

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

    return journeyManager.performTransition(Events.SEARCH_PHYSICAL_GOODS_SPECIAL);
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
    String formSearchTerm = form.itemName + ", " + form.description ;
    if (form.isComponent != null && form.isComponent && StringUtils.isNotBlank(form.component)) {
     formSearchTerm = formSearchTerm + ", " + form.component;
    }
    if (form.partNumber != null && StringUtils.isNotBlank(form.partNumber)) {
      formSearchTerm = formSearchTerm + ", " + form.partNumber;
    }
    return formSearchTerm;
  }

  public static class SearchForm {

    @Required(message = "Select whether this is a component or part of something else")
    public Boolean isComponent;

    @Required(message = "Enter a description of the item")
    public String description;

    @Required(message = "Enter the item name")
    public String itemName;

    @MaxLength(value = 500, message = "Partnumber cannot exceed 500 characters")
    public String partNumber;

    @MaxLength(value = 4000, message = "Component description cannot exceed 4000 characters")
    public String component;

  }

}