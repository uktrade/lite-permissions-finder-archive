package controllers.ogel;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.ogels.conditions.OgelConditionsServiceClient;
import components.services.ogels.virtualeu.VirtualEUOgelClient;
import journey.Events;
import models.OgelActivityType;
import models.VirtualEUOgelStage;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import utils.CountryUtils;
import views.html.ogel.ogelQuestions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OgelQuestionsController {

  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final JourneyManager journeyManager;
  private final VirtualEUOgelClient virtualEUOgelClient;
  private final OgelConditionsServiceClient ogelConditionsServiceClient;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public OgelQuestionsController(JourneyManager journeyManager,
                                 FormFactory formFactory,
                                 PermissionsFinderDao permissionsFinderDao,
                                 VirtualEUOgelClient virtualEUOgelClient,
                                 OgelConditionsServiceClient ogelConditionsServiceClient,
                                 HttpExecutionContext httpExecutionContext) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.virtualEUOgelClient = virtualEUOgelClient;
    this.ogelConditionsServiceClient = ogelConditionsServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  public Result renderForm() {
    Optional<OgelQuestionsForm> templateFormOptional = permissionsFinderDao.getOgelQuestionsForm();
    OgelQuestionsForm templateForm = templateFormOptional.isPresent() ? templateFormOptional.get() : new OgelQuestionsForm();
    return ok(ogelQuestions.render(formFactory.form(OgelQuestionsForm.class).fill(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<OgelQuestionsForm> form = formFactory.form(OgelQuestionsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(ogelQuestions.render(form)));
    }
    else {
      OgelQuestionsForm ogelQuestionsForm = form.get();
      permissionsFinderDao.saveOgelQuestionsForm(ogelQuestionsForm);
      return getNextStage();
    }
  }

  private CompletionStage<Result> getNextStage() {
    String controlCode = permissionsFinderDao.getControlCodeForRegistration();
    String sourceCountry = permissionsFinderDao.getSourceCountry();
    List<String> destinationCountries = CountryUtils.getDestinationCountries(
        permissionsFinderDao.getFinalDestinationCountry(), permissionsFinderDao.getThroughDestinationCountries());

    return virtualEUOgelClient.sendServiceRequest(controlCode, sourceCountry, destinationCountries)
        .thenApplyAsync((result) -> {
          if (result.isVirtualEu()) {
            permissionsFinderDao.saveOgelId(result.getOgelId());
            return ogelConditionsServiceClient.get(result.getOgelId(), controlCode)
                .thenApplyAsync(conditionsResult -> {
                  if (!conditionsResult.isEmpty) {
                    return journeyManager.performTransition(Events.VIRTUAL_EU_OGEL_STAGE,
                        VirtualEUOgelStage.VIRTUAL_EU_WITH_CONDITIONS);
                  }
                  else {
                    return journeyManager.performTransition(Events.VIRTUAL_EU_OGEL_STAGE,
                        VirtualEUOgelStage.VIRTUAL_EU_WITHOUT_CONDITIONS);
                  }
                }, httpExecutionContext.current()).thenCompose(Function.identity());
          }
          else {
            return journeyManager.performTransition(Events.VIRTUAL_EU_OGEL_STAGE, VirtualEUOgelStage.NO_VIRTUAL_EU);
          }
        }, httpExecutionContext.current())
        .thenCompose(Function.identity());
  }

  public static class OgelQuestionsForm {

    @Required(message = "Select whether you are exporting goods for or after repair or replacement")
    public String forRepairReplacement;

    @Required(message = "Select whether you are exporting goods for or after exhibition or demonstration")
    public String forExhibitionDemonstration;

    public static List<String> formToActivityTypes(Optional<OgelQuestionsForm> ogelQuestionsFormOptional) {
      // TODO account for TECH
      Map<OgelActivityType, String> activityMap = new HashMap<>();
      activityMap.put(OgelActivityType.DU_ANY, OgelActivityType.DU_ANY.value());
      activityMap.put(OgelActivityType.EXHIBITION, OgelActivityType.EXHIBITION.value());
      activityMap.put(OgelActivityType.MIL_ANY, OgelActivityType.MIL_ANY.value());
      activityMap.put(OgelActivityType.MIL_GOV, OgelActivityType.MIL_GOV.value());
      activityMap.put(OgelActivityType.REPAIR, OgelActivityType.REPAIR.value());

      if (ogelQuestionsFormOptional.isPresent()) {
        OgelQuestionsForm ogelQuestionsForm = ogelQuestionsFormOptional.get();
        if ("false".equals(ogelQuestionsForm.forRepairReplacement)) {
          activityMap.remove(OgelActivityType.REPAIR);
        }
        if ("false".equals(ogelQuestionsForm.forExhibitionDemonstration)) {
          activityMap.remove(OgelActivityType.EXHIBITION);
        }
      }

      // Returns a list of values
      return activityMap.entrySet().stream()
          .map(es -> es.getValue())
          .collect(Collectors.toList());
    }
  }

}