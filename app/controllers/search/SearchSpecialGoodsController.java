package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import controllers.search.enums.GoodsSpecialisation;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.controlcode.ControlCodeSubJourney;
import models.search.SearchSpecialGoodsDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.search.searchSpeciallyModified;

import java.util.concurrent.CompletionStage;

public class SearchSpecialGoodsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public SearchSpecialGoodsController(JourneyManager journeyManager,
                                      FormFactory formFactory,
                                      PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public CompletionStage<Result> renderForm() {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveContextToSubJourney();
    return renderFormInternal(controlCodeSubJourney);
  }

  public CompletionStage<Result> renderFormInternal(ControlCodeSubJourney controlCodeSubJourney) {
    SearchSpecialGoodsForm templateForm = new SearchSpecialGoodsForm();

    if(permissionsFinderDao.getGoodsSpecialisation().isPresent()) {
      templateForm.isSpecialItemOrComponent = permissionsFinderDao.getGoodsSpecialisation().get().getPrompt();
    }
    Boolean isItem = permissionsFinderDao.getPhysicalGoodsSearchForm(controlCodeSubJourney).get().isItem;

    return completedFuture(ok(searchSpeciallyModified.render(formFactory.form(SearchSpecialGoodsForm.class).fill(templateForm),
        new SearchSpecialGoodsDisplay(isItem), GoodsSpecialisation.getSelectOptions())));
  }

  public CompletionStage<Result> handleSubmit() {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveContextToSubJourney();
    return handleSubmitInternal(controlCodeSubJourney);
  }

  public CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Form<SearchSpecialGoodsForm> form = formFactory.form(SearchSpecialGoodsForm.class).bindFromRequest();
    Boolean isComponent = permissionsFinderDao.getPhysicalGoodsSearchForm(controlCodeSubJourney).get().isItem;
    if (form.hasErrors()) {
      return completedFuture(ok(searchSpeciallyModified.render(form, new SearchSpecialGoodsDisplay(isComponent), GoodsSpecialisation.getSelectOptions())));
    }
    permissionsFinderDao.saveGoodsSpecialisation(GoodsSpecialisation.valueOf(form.get().isSpecialItemOrComponent));
    return journeyManager.performTransition(Events.SEARCH_PHYSICAL_GOODS);
  }

  public static class SearchSpecialGoodsForm {

    @Required(message = "Select whether this item is specifically designed or modified")
    public String isSpecialItemOrComponent;

  }

}