package journey;

import components.common.journey.GraphvizSerialiser;
import components.common.journey.JourneyDefinition;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;

public class JourneyPrint {

  @Test
  public void printExportJourney() {
    Collection<JourneyDefinition> journeyDefinitions = new ExportJourneyDefinitionBuilder(new ControlCodeDecider(null, null)).buildAll();
    Optional<JourneyDefinition> serialiseJourney = journeyDefinitions.stream().filter(j -> JourneyDefinitionNames.EXPORT.equals(j.getJourneyName())).findFirst();

    System.out.println(new GraphvizSerialiser().generateGraphvizSyntax(serialiseJourney.get()));
  }
}
