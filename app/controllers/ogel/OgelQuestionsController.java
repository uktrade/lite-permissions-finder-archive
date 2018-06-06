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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OgelQuestionsController {

  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;
  private final JourneyManager journeyManager;
  private final VirtualEUOgelClient virtualEuClient;
  private final OgelConditionsServiceClient conditionsClient;
  private final HttpExecutionContext httpContext;
  private final views.html.ogel.ogelQuestions ogelQuestions;

  @Inject
  public OgelQuestionsController(JourneyManager journeyManager,
                                 FormFactory formFactory,
                                 PermissionsFinderDao dao,
                                 VirtualEUOgelClient virtualEuClient,
                                 OgelConditionsServiceClient conditionsClient,
                                 HttpExecutionContext httpContext, views.html.ogel.ogelQuestions ogelQuestions) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.dao = dao;
    this.virtualEuClient = virtualEuClient;
    this.conditionsClient = conditionsClient;
    this.httpContext = httpContext;
    this.ogelQuestions = ogelQuestions;
  }

  public Result renderForm() {
    Optional<OgelQuestionsForm> optForm = dao.getOgelQuestionsForm();
    return ok(ogelQuestions.render(formFactory.form(OgelQuestionsForm.class).fill(optForm.orElseGet(OgelQuestionsForm::new))));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<OgelQuestionsForm> form = formFactory.form(OgelQuestionsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(ogelQuestions.render(form)));
    } else {
      OgelQuestionsForm ogelQuestionsForm = form.get();
      dao.saveOgelQuestionsForm(ogelQuestionsForm);
      return getNextStage();
    }
  }

  private CompletionStage<Result> getNextStage() {
    String controlCode = dao.getControlCodeForRegistration();
    String sourceCountry = dao.getSourceCountry();
    List<String> destinationCountries = CountryUtils.getDestinationCountries(dao.getFinalDestinationCountry(), dao.getThroughDestinationCountries());

    return virtualEuClient.sendServiceRequest(controlCode, sourceCountry, destinationCountries)
        .thenApplyAsync((result) -> {
          if (result.isVirtualEu()) {
            dao.saveOgelId(result.getOgelId());
            return conditionsClient.get(result.getOgelId(), controlCode)
                .thenApplyAsync(conditionsResult -> {
                  if (!conditionsResult.isEmpty) {
                    return journeyManager.performTransition(Events.VIRTUAL_EU_OGEL_STAGE, VirtualEUOgelStage.VIRTUAL_EU_WITH_CONDITIONS);
                  } else {
                    return journeyManager.performTransition(Events.VIRTUAL_EU_OGEL_STAGE, VirtualEUOgelStage.VIRTUAL_EU_WITHOUT_CONDITIONS);
                  }
                }, httpContext.current()).thenCompose(Function.identity());
          } else {
            return journeyManager.performTransition(Events.VIRTUAL_EU_OGEL_STAGE, VirtualEUOgelStage.NO_VIRTUAL_EU);
          }
        }, httpContext.current())
        .thenCompose(Function.identity());
  }

  public static class OgelQuestionsForm {

    @Required(message = "Select whether you are exporting goods for or after repair or replacement")
    public String forRepair;

    @Required(message = "Select whether you are exporting goods for or after exhibition or demonstration")
    public String forExhibition;

    @Required(message = "Select whether your goods were manufactured before 1897, or are worth less than £30,000")
    public String beforeOrLess;

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
        if ("false".equals(ogelQuestionsForm.forRepair)) {
          activityMap.remove(OgelActivityType.REPAIR);
        }
        if ("false".equals(ogelQuestionsForm.forExhibition)) {
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