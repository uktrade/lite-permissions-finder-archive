package controllers.modal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import components.common.cache.CountryProvider;
import components.common.client.OgelServiceClient;
import components.persistence.LicenceFinderDao;
import controllers.licencefinder.DestinationController;
import controllers.licencefinder.QuestionsController;
import exceptions.UnknownParameterException;
import models.TradeType;
import models.view.QuestionView;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import play.mvc.Result;
import triage.config.ControllerConfigService;
import triage.config.DefinitionConfig;
import triage.text.*;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;

public class ModalSelectionController {

  private static final String CONTROL_CODE_QUESTION = "What control list entry describes your items?";
  private static final String GOODS_GOING_QUESTION = "Where are your items going?";
  private static final String FIRST_COUNTRY = "First country or territory that will receive the items";
  private static final String REPAIR_QUESTION = "Are you exporting goods for or after repair or replacement?";
  private static final String EXHIBITION_QUESTION = "Are you exporting goods for or after exhibition or demonstration?";
  private static final String BEFORE_OR_LESS_QUESTION = "Were your goods manufactured before 1897 and are they worth less than £35,000?";

  private static final String YES = "Yes";
  private static final String NO = "No";

  private final HtmlRenderService htmlRenderService;
  private final ControllerConfigService controllerConfigService;
  private final views.html.modal.modalDefinitionView modalDefinitionView;
  private final OgelServiceClient ogelServiceClient;
  private final LicenceFinderDao licenceFinderDao;
  private final CountryProvider countryProvider;

  @Inject
  public ModalSelectionController(HtmlRenderService htmlRenderService,
                                  ControllerConfigService controllerConfigService,
                                  views.html.modal.modalDefinitionView modalDefinitionView,
                                  OgelServiceClient ogelServiceClient,
                                  LicenceFinderDao licenceFinderDao,
                                  @Named("countryProviderExport") CountryProvider countryProvider) {
    this.htmlRenderService = htmlRenderService;
    this.controllerConfigService = controllerConfigService;
    this.modalDefinitionView = modalDefinitionView;
    this.ogelServiceClient = ogelServiceClient;
    this.licenceFinderDao = licenceFinderDao;
    this.countryProvider = countryProvider;
  }

  public Result renderPastSelections(String sessionId) {
    return ok(Json.prettyPrint(new ObjectMapper().valueToTree(getLicenceFinderAnswers(sessionId))));
  }

  /*public Result renderPastSelectionsView(String sessionId) {
    return ok(modalDefinitionView.render(getLicenceFinderAnswers(sessionId));
  }*/

  private List<QuestionView> getLicenceFinderAnswers(String sessionId) {
    List<QuestionView> views = new ArrayList<>();

    String controlCode = licenceFinderDao.getControlCode(sessionId)
      .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    views.add(new QuestionView(CONTROL_CODE_QUESTION, controlCode));

    TradeType tradeType = licenceFinderDao.getTradeType(sessionId)
      .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    views.add(new QuestionView(GOODS_GOING_QUESTION, tradeType.getTitle()));

    String destinationCountry = licenceFinderDao.getDestinationCountry(sessionId)
      .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    String destinationCountryName = countryProvider.getCountry(destinationCountry).getCountryName();
    views.add(new QuestionView(DestinationController.DESTINATION_QUESTION, destinationCountryName));

    boolean multipleCountries = licenceFinderDao.getMultipleCountries(sessionId)
      .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    views.add(new QuestionView(DestinationController.DESTINATION_MULTIPLE_QUESTION, toAnswer(multipleCountries)));
    if (multipleCountries) {
      String firstConsigneeCountry = licenceFinderDao.getFirstConsigneeCountry(sessionId)
        .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
      String firstConsigneeCountryName = countryProvider.getCountry(firstConsigneeCountry).getCountryName();
      views.add(new QuestionView(FIRST_COUNTRY, firstConsigneeCountryName));
    }

    QuestionsController.QuestionsForm questionsForm = licenceFinderDao.getQuestionsForm(sessionId)
      .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    views.add(new QuestionView(REPAIR_QUESTION, toAnswer(questionsForm.forRepair)));
    views.add(new QuestionView(EXHIBITION_QUESTION, toAnswer(questionsForm.forExhibition)));
    views.add(new QuestionView(BEFORE_OR_LESS_QUESTION, toAnswer(questionsForm.beforeOrLess)));
    return views;
  }

  private String toAnswer(boolean bool) {
    return bool ? YES : NO;
  }

}
