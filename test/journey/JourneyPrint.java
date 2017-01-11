package journey;

import components.common.journey.GraphvizSerialiser;
import components.common.journey.JourneyDefinition;
import journey.deciders.CatchallControlsDecider;
import journey.deciders.CategoryControlsDecider;
import journey.deciders.ControlCodeDecider;
import journey.deciders.ExportCategoryDecider;
import journey.deciders.RelatedControlsDecider;
import journey.deciders.RelationshipWithSoftwareDecider;
import journey.deciders.RelationshipWithTechnologyDecider;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class JourneyPrint {

  @Test
  public void printExportJourney() {
    Collection<JourneyDefinition> journeyDefinitions = new ExportJourneyDefinitionBuilder(
        new ControlCodeDecider(null, null),
        new ExportCategoryDecider(null),
        new CategoryControlsDecider(null, null, null, null),
        new RelatedControlsDecider(null, null, null, null),
        new CatchallControlsDecider(null, null, null, null),
        new RelationshipWithTechnologyDecider(null, null),
        new RelationshipWithSoftwareDecider(null, null)
    ).buildAll();
    Optional<JourneyDefinition> serialiseJourney = journeyDefinitions.stream().filter(j -> JourneyDefinitionNames.EXPORT.equals(j.getJourneyName())).findFirst();

    System.out.println(new GraphvizSerialiser().generateGraphvizSyntax(Collections.singletonList(serialiseJourney.get())));
  }
}
