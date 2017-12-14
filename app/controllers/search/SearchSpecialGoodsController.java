package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import controllers.search.enums.SpecialGoods;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.controlcode.ControlCodeSubJourney;
import models.search.SpeciallyModifiedDisplay;
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
  private Boolean isComponent;

  @Inject
  public SearchSpecialGoodsController(JourneyManager journeyManager,
                                      FormFactory formFactory,
                                      PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public CompletionStage<Result> renderForm() {
    SpeciallyModifiedSearchForm templateForm = new SpeciallyModifiedSearchForm();
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveContextToSubJourney();
    isComponent = permissionsFinderDao.getPhysicalGoodsSearchForm(controlCodeSubJourney).get().isComponent;

    return completedFuture(ok(searchSpeciallyModified.render(formFactory.form(SpeciallyModifiedSearchForm.class).fill(templateForm),
        new SpeciallyModifiedDisplay(isComponent), SpecialGoods.getSelectOptions())));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<SpeciallyModifiedSearchForm> form = formFactory.form(SpeciallyModifiedSearchForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(searchSpeciallyModified.render(form, new SpeciallyModifiedDisplay(isComponent), SpecialGoods.getSelectOptions())));
    }
    permissionsFinderDao.saveIsItemModifiedOrDesigned(SpecialGoods.valueOf(form.get().isSpecialItemOrComponent));
    return journeyManager.performTransition(Events.SEARCH_PHYSICAL_GOODS);
  }

  public static class SpeciallyModifiedSearchForm {

    @Required(message = "Select whether this item is specifically designed or modified")
    public String isSpecialItemOrComponent;

  }

}