package importcontent;

import components.common.journey.BackLink;
import components.common.journey.JourneyDefinitionBuilder;
import components.common.journey.JourneyStage;
import controllers.importcontent.ImportEvents;
import controllers.importcontent.StaticController;
import controllers.routes;
import importcontent.models.ImportFoodWhat;
import importcontent.models.ImportWhat;
import importcontent.models.ImportWhere;
import importcontent.models.ImportYesNo;
import journey.JourneyDefinitionNames;

public class ImportJourneyDefinitionBuilder extends JourneyDefinitionBuilder {

  public static final String KEY_WHERE = "WHERE";
  public static final String KEY_WHERE_QUESTION = "Where are you importing from?";
  public static final String KEY_WHAT = "WHAT";
  public static final String KEY_WHAT_QUESTION = "What are you importing?";
  public static final String KEY_CHARCOAL = "CHARCOAL";
  public static final String KEY_CHARCOAL_QUESTION = "Are you importing charcoal or charcoal products?";
  public static final String KEY_MILITARY = "MILITARY";
  public static final String KEY_MILITARY_QUESTION = "Are you importing military goods or technology?";
  public static final String KEY_SHOT = "SHOT";
  public static final String KEY_SHOT_QUESTION = "Are you importing single-shot rifles or shotguns?";
  public static final String KEY_SUBSTANCES = "SUBSTANCES";
  public static final String KEY_SUBSTANCES_QUESTION = "Are you importing substances that potentially cause cancer, eg asbestos?";
  public static final String KEY_OZONE = "OZONE";
  public static final String KEY_OZONE_QUESTION = "Are you importing ozone-depleting substances?";
  public static final String KEY_DRUGS = "DRUGS";
  public static final String KEY_DRUGS_QUESTION = "Are you importing controlled drugs?";
  public static final String KEY_FOOD_WHAT = "FOOD_WHAT";
  public static final String KEY_FOOD_WHAT_QUESTION = "What are you importing?";
  public static final String KEY_ENDANGERED = "ENDANGERED";
  public static final String KEY_ENDANGERED_QUESTION = "Are the animals endangered?";

  private JourneyStage importWhere = defineStage(KEY_WHERE, KEY_WHERE_QUESTION, controllers.importcontent.routes.ImportController.renderForm(KEY_WHERE));
  private JourneyStage importWhat = defineStage(KEY_WHAT, KEY_WHAT_QUESTION, controllers.importcontent.routes.ImportController.renderForm(KEY_WHAT));
  private JourneyStage importCharcoal = defineStage(KEY_CHARCOAL, KEY_CHARCOAL_QUESTION, controllers.importcontent.routes.ImportController.renderForm(KEY_CHARCOAL));
  private JourneyStage importMilitary = defineStage(KEY_MILITARY, KEY_MILITARY_QUESTION, controllers.importcontent.routes.ImportController.renderForm(KEY_MILITARY));
  private JourneyStage importShot = defineStage(KEY_SHOT, KEY_SHOT_QUESTION, controllers.importcontent.routes.ImportController.renderForm(KEY_SHOT));
  private JourneyStage importSubstances = defineStage(KEY_SUBSTANCES, KEY_SUBSTANCES_QUESTION, controllers.importcontent.routes.ImportController.renderForm(KEY_SUBSTANCES));
  private JourneyStage importOzone = defineStage(KEY_OZONE, KEY_OZONE_QUESTION, controllers.importcontent.routes.ImportController.renderForm(KEY_OZONE));
  private JourneyStage importDrugs = defineStage(KEY_DRUGS, KEY_DRUGS_QUESTION, controllers.importcontent.routes.ImportController.renderForm(KEY_DRUGS));
  private JourneyStage importFoodWhat = defineStage(KEY_FOOD_WHAT, KEY_FOOD_WHAT_QUESTION, controllers.importcontent.routes.ImportController.renderForm(KEY_FOOD_WHAT));
  private JourneyStage importEndangered = defineStage(KEY_ENDANGERED, KEY_ENDANGERED_QUESTION, controllers.importcontent.routes.ImportController.renderForm(KEY_ENDANGERED));

  private JourneyStage importEp1 = defineStage(StaticController.EP1_KEY, StaticController.EP1_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP1.name()));
  private JourneyStage importEp2 = defineStage(StaticController.EP2_KEY, StaticController.EP2_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP2.name()));
  private JourneyStage importEp3 = defineStage(StaticController.EP3_KEY, StaticController.EP3_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP3.name()));
  private JourneyStage importEp4 = defineStage(StaticController.EP4_KEY, StaticController.EP4_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP4.name()));
  private JourneyStage importEp5 = defineStage(StaticController.EP5_KEY, StaticController.EP5_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP5.name()));
  private JourneyStage importEp6 = defineStage(StaticController.EP6_KEY, StaticController.EP6_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP6.name()));
  private JourneyStage importEp7 = defineStage(StaticController.EP7_KEY, StaticController.EP7_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP7.name()));
  private JourneyStage importEp8 = defineStage(StaticController.EP8_KEY, StaticController.EP8_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP8.name()));
  private JourneyStage importEp9 = defineStage(StaticController.EP9_KEY, StaticController.EP9_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP9.name()));
  private JourneyStage importEp10 = defineStage(StaticController.EP10_KEY, StaticController.EP10_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP10.name()));
  private JourneyStage importEp11 = defineStage(StaticController.EP11_KEY, StaticController.EP11_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP11.name()));
  private JourneyStage importEp12 = defineStage(StaticController.EP12_KEY, StaticController.EP12_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP12.name()));
  private JourneyStage importEp13 = defineStage(StaticController.EP13_KEY, StaticController.EP13_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP13.name()));
  private JourneyStage importEp14 = defineStage(StaticController.EP14_KEY, StaticController.EP14_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP14.name()));
  private JourneyStage importEp15 = defineStage(StaticController.EP15_KEY, StaticController.EP15_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP15.name()));
  private JourneyStage importEp16 = defineStage(StaticController.EP16_KEY, StaticController.EP16_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP16.name()));
  private JourneyStage importEp17 = defineStage(StaticController.EP17_KEY, StaticController.EP17_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP17.name()));
  private JourneyStage importEp18 = defineStage(StaticController.EP18_KEY, StaticController.EP18_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP18.name()));
  private JourneyStage importEp19 = defineStage(StaticController.EP19_KEY, StaticController.EP19_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP19.name()));
  private JourneyStage importEp20 = defineStage(StaticController.EP20_KEY, StaticController.EP20_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP20.name()));
  private JourneyStage importEp21 = defineStage(StaticController.EP21_KEY, StaticController.EP21_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP21.name()));
  private JourneyStage importEp22 = defineStage(StaticController.EP22_KEY, StaticController.EP22_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP22.name()));
  private JourneyStage importEp23 = defineStage(StaticController.EP23_KEY, StaticController.EP23_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP23.name()));
  private JourneyStage importEp24 = defineStage(StaticController.EP24_KEY, StaticController.EP24_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP24.name()));
  private JourneyStage importEp25 = defineStage(StaticController.EP25_KEY, StaticController.EP25_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP25.name()));
  private JourneyStage importEp26 = defineStage(StaticController.EP26_KEY, StaticController.EP26_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP26.name()));
  private JourneyStage importEp27 = defineStage(StaticController.EP27_KEY, StaticController.EP27_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP27.name()));
  private JourneyStage importEp28 = defineStage(StaticController.EP28_KEY, StaticController.EP28_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP28.name()));
  private JourneyStage importEp29 = defineStage(StaticController.EP29_KEY, StaticController.EP29_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP29.name()));
  private JourneyStage importEp30 = defineStage(StaticController.EP30_KEY, StaticController.EP30_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP30.name()));
  private JourneyStage importEp31 = defineStage(StaticController.EP31_KEY, StaticController.EP31_TITLE, controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.IMPORT_EP31.name()));

  private JourneyStage notImplemented = defineStage("notImplemented", "This section is currently under development", controllers.importcontent.routes.StaticController.render(StaticController.ImportHtml.NOT_IMPLEMENTED.name()));

  @Override
  protected void journeys() {

    defineJourney(JourneyDefinitionNames.IMPORT, importWhere, BackLink.to(routes.TradeTypeController.renderForm(), "Where are your items going?"));

    // Where are you importing from?
    atStage(importWhere)
        .onEvent(ImportEvents.IMPORT_WHERE_SELECTED)
        .branch()
        .when(ImportWhere.OTHER, moveTo(importWhat))
        .when(ImportWhere.CRIMEA, moveTo(importEp3))
        .when(ImportWhere.EU, moveTo(importEp15))
        .when(ImportWhere.SOMALIA, moveTo(importCharcoal))
        .when(ImportWhere.SYRIA, moveTo(importEp6))
        .when(ImportWhere.RUSSIA, moveTo(importMilitary))
        .when(ImportWhere.IRAN, moveTo(importMilitary))
        .when(ImportWhere.MYANMAR, moveTo(importMilitary));

    // Are you importing charcoal or charcoal products?
    atStage(importCharcoal)
        .onEvent(ImportEvents.IMPORT_CHARCOAL_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(importEp5))
        .when(ImportYesNo.NO, moveTo(importWhat));

    // Are you importing military goods or technology?
    // need to extend for different 'yes' endpoints for 'Iran', 'Russia' and 'Myanmar'
    // importEp1 is 'Iran' endpoint
    atStage(importMilitary)
        .onEvent(ImportEvents.IMPORT_MILITARY_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(importEp1))
        .when(ImportYesNo.NO, moveTo(importWhat));

    // What are you importing?
    atStage(importWhat)
        .onEvent(ImportEvents.IMPORT_WHAT_SELECTED)
        .branch()
        .when(ImportWhat.FIREARMS, moveTo(importShot))
        .when(ImportWhat.TEXTILES, moveTo(notImplemented))
        .when(ImportWhat.IRON, moveTo(notImplemented))
        .when(ImportWhat.FOOD, moveTo(importFoodWhat))
        .when(ImportWhat.MEDICINES, moveTo(importDrugs))
        .when(ImportWhat.NUCLEAR, moveTo(importEp16))
        .when(ImportWhat.EXPLOSIVES, moveTo(importEp17))
        .when(ImportWhat.DIAMONDS, moveTo(importEp20))
        .when(ImportWhat.TORTURE, moveTo(importEp21))
        .when(ImportWhat.LAND_MINES, moveTo(importEp22))
        .when(ImportWhat.CHEMICALS, moveTo(importSubstances))
        .when(ImportWhat.NONE_ABOVE, moveTo(importEp31));

    // Are you importing single-shot rifles or shotguns?
    atStage(importShot)
        .onEvent(ImportEvents.IMPORT_SHOT_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(importEp7))
        .when(ImportYesNo.NO, moveTo(importEp8));

    // Are you importing substances that potentially cause cancer, eg asbestos?
    atStage(importSubstances)
        .onEvent(ImportEvents.IMPORT_SUBSTANCES_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(importEp28))
        .when(ImportYesNo.NO, moveTo(importOzone));

    // Are you importing ozone-depleting substances?
    atStage(importOzone)
        .onEvent(ImportEvents.IMPORT_OZONE_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(importEp29))
        .when(ImportYesNo.NO, moveTo(importEp30));

    // Are you importing controlled drugs?
    atStage(importDrugs)
        .onEvent(ImportEvents.IMPORT_DRUGS_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(importEp18))
        .when(ImportYesNo.NO, moveTo(importEp19));

    // What are you importing? (food)
    atStage(importFoodWhat)
        .onEvent(ImportEvents.IMPORT_FOOD_WHAT_SELECTED)
        .branch()
        .when(ImportFoodWhat.FOOD, moveTo(importEp24))
        .when(ImportFoodWhat.NON_FOOD, moveTo(importEp25))
        .when(ImportFoodWhat.ANIMALS, moveTo(importEndangered))
        .when(ImportFoodWhat.NON_EDIBLE, moveTo(importEp23));

    // Are the animals endangered?
    atStage(importEndangered)
        .onEvent(ImportEvents.IMPORT_ENDANGERED_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(importEp26))
        .when(ImportYesNo.NO, moveTo(importEp27));
  }
}
