package journey;

import com.google.inject.Inject;
import components.common.journey.JourneyDefinitionBuilder;
import components.common.journey.JourneyStage;

public class ExportJourneyDefinitionBuilder extends JourneyDefinitionBuilder {

  private final JourneyStage tradeForm = defineStage("destinationCountries", controllers.licencefinder.routes.TradeController.renderTradeForm("sessionId"));

  @Inject
  public ExportJourneyDefinitionBuilder() {
  }

  @Override
  protected void journeys() {
    defineJourney("", tradeForm);
  }
}
