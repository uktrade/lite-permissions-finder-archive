package controllers.search;

import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.search.SearchServiceClient;
import controllers.ErrorController;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchController {

  protected final JourneyManager journeyManager;
  private final FormFactory formFactory;
  protected final PermissionsFinderDao permissionsFinderDao;
  protected final HttpExecutionContext httpExecutionContext;
  protected final SearchServiceClient searchServiceClient;
  protected final ErrorController errorController;

  public SearchController(JourneyManager journeyManager,
                          FormFactory formFactory,
                          PermissionsFinderDao permissionsFinderDao,
                          HttpExecutionContext httpExecutionContext,
                          SearchServiceClient searchServiceClient,
                          ErrorController errorController) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.searchServiceClient = searchServiceClient;
    this.errorController = errorController;
  }

  public Form<ControlCodeSearchForm> searchForm(ControlCodeSearchForm templateForm) {
    return formFactory.form(ControlCodeSearchForm.class).fill(templateForm);
  }

  public Form<ControlCodeSearchForm> searchForm() {
    return formFactory.form(ControlCodeSearchForm.class);
  }

  public Form<ControlCodeSearchForm> bindSearchForm() {
    // Trims whitespace from the form fields
    Map<String, String> map = searchForm().bindFromRequest().data().entrySet()
        .stream()
        .map(entry -> {
          if (entry.getValue() != null) {
            entry.setValue(entry.getValue().trim());
          }
          return entry;
        })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return searchForm().bind(map);
  }

  public static String getSearchTerms(ControlCodeSearchForm form) {
    return Arrays.asList(
        form.description,
        form.component,
        form.brand,
        form.partNumber
    ).stream()
        .filter(fieldValue -> StringUtils.isNoneBlank(fieldValue))
        .collect(Collectors.joining(", "));
  }

  public static class ControlCodeSearchForm {

    @Required(message = "You must enter a description of your goods")
    public String description;

    public String component;

    public String brand;

    public String partNumber;

  }
}
