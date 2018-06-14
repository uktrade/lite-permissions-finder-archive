package journey;

import com.google.inject.Inject;
import components.common.journey.BackLink;
import components.common.journey.JourneyDefinitionBuilder;
import components.common.journey.JourneyStage;
import controllers.routes;


public class ExportJourneyDefinitionBuilder extends JourneyDefinitionBuilder {

  private final JourneyStage destinationCountries = defineStage("destinationCountries",
      routes.DestinationCountryController.renderForm());
  private final JourneyStage tradeType = defineStage("tradeType",
      routes.TradeTypeController.renderForm());

  @Inject
  public ExportJourneyDefinitionBuilder() {

  }

  @Override
  protected void journeys() {
    atStage(tradeType)
        .onEvent(Events.EXPORT_TRADE_TYPE)
        .then(moveTo(destinationCountries));

    ogelStages();

    defineJourney(JourneyDefinitionNames.EXPORT, tradeType);
    //defineJourney(TestEntryController.JOURNEY_NAME, tradeType);

    // TODO control code hard coded, link back to tradeType for now
    defineJourney(JourneyDefinitionNames.CHANGE_CONTROL_CODE, tradeType,
        BackLink.to(routes.SummaryController.renderForm(), "Back"));
    defineJourney(JourneyDefinitionNames.CHANGE_DESTINATION_COUNTRIES, destinationCountries,
        BackLink.to(routes.SummaryController.renderForm(), "Back"));
  }

  private void ogelStages() {

    JourneyStage virtualEU = defineStage("virtualEU",
        routes.StaticContentController.renderVirtualEU());

  }
}
