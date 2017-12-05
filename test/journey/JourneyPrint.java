package journey;

import components.common.journey.GraphvizSerialiser;
import components.common.journey.JourneyDefinition;
import importcontent.ImportJourneyDefinitionBuilder;
import journey.deciders.CatchallControlsDecider;
import journey.deciders.CategoryControlsDecider;
import journey.deciders.RelatedControlsDecider;
import journey.deciders.RelationshipWithSoftwareDecider;
import journey.deciders.RelationshipWithTechnologyDecider;
import journey.deciders.controlcode.AdditionalSpecificationsDecider;
import journey.deciders.controlcode.DecontrolsDecider;
import journey.deciders.controlcode.TechnicalNotesDecider;
import journey.deciders.relatedcodes.CatchallRelatedControlsDecider;
import journey.deciders.relatedcodes.CategoryRelatedControlsDecider;
import journey.deciders.relatedcodes.NonExemptRelatedControlsDecider;
import journey.deciders.relatedcodes.RelatedRelatedControlsDecider;
import journey.deciders.relatedcodes.SearchRelatedControlsDecider;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class JourneyPrint {

  @Test
  public void printExportJourney() {
    Collection<JourneyDefinition> journeyDefinitions = new ExportJourneyDefinitionBuilder(
        new AdditionalSpecificationsDecider(null, null, null),
        new DecontrolsDecider(null, null, null),
        new TechnicalNotesDecider(null, null, null),
        new CategoryControlsDecider(null, null, null, null),
        new RelatedControlsDecider(null, null, null, null),
        new CatchallControlsDecider(null, null, null, null),
        new RelationshipWithTechnologyDecider(null, null, null),
        new RelationshipWithSoftwareDecider(null, null, null),
        new SearchRelatedControlsDecider(null, null, null, null),
        new CatchallRelatedControlsDecider(null, null, null, null),
        new CategoryRelatedControlsDecider(null, null, null, null),
        new RelatedRelatedControlsDecider(null, null, null, null),
        new NonExemptRelatedControlsDecider(null, null, null, null)
    ).buildAll();
    Optional<JourneyDefinition> serialiseJourney = journeyDefinitions.stream().filter(j -> JourneyDefinitionNames.EXPORT.equals(j.getJourneyName())).findFirst();

    System.out.println(new GraphvizSerialiser().generateGraphvizSyntax(Collections.singletonList(serialiseJourney.get())));
  }

  @Test
  public void printImportJourney() {
    Collection<JourneyDefinition> journeyDefinitions = new ImportJourneyDefinitionBuilder(null).buildAll();
    Optional<JourneyDefinition> serialiseJourney = journeyDefinitions.stream().filter(j -> JourneyDefinitionNames.IMPORT.equals(j.getJourneyName())).findFirst();

    System.out.println(new GraphvizSerialiser().generateGraphvizSyntax(Collections.singletonList(serialiseJourney.get())));
  }
}
