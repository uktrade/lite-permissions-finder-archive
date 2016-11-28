package importcontent;

import components.common.journey.ParameterisedJourneyEvent;
import importcontent.models.ImportFoodWhat;
import importcontent.models.ImportWhat;
import importcontent.models.ImportWhatWhereIron;
import importcontent.models.ImportWhatWhereTextiles;
import importcontent.models.ImportWhere;
import importcontent.models.ImportYesNo;

public class ImportEvents {

  public static final ParameterisedJourneyEvent<ImportWhat> IMPORT_WHAT_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_WHAT_SELECTED", ImportWhat.class);
  public static final ParameterisedJourneyEvent<ImportWhere> IMPORT_WHERE_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_WHERE_SELECTED", ImportWhere.class);
  public static final ParameterisedJourneyEvent<ImportYesNo> IMPORT_CHARCOAL_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_CHARCOAL_SELECTED", ImportYesNo.class);

  public static final ParameterisedJourneyEvent<ImportYesNo> IMPORT_MILITARY_IRAN_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_MILITARY_IRAN_SELECTED", ImportYesNo.class);
  public static final ParameterisedJourneyEvent<ImportYesNo> IMPORT_MILITARY_RUSSIA_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_MILITARY_RUSSIA_SELECTED", ImportYesNo.class);
  public static final ParameterisedJourneyEvent<ImportYesNo> IMPORT_MILITARY_MYANMAR_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_MILITARY_MYANMAR_SELECTED", ImportYesNo.class);

  public static final ParameterisedJourneyEvent<ImportYesNo> IMPORT_SHOT_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_SHOT_SELECTED", ImportYesNo.class);
  public static final ParameterisedJourneyEvent<ImportYesNo> IMPORT_SUBSTANCES_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_SUBSTANCES_SELECTED", ImportYesNo.class);
  public static final ParameterisedJourneyEvent<ImportYesNo> IMPORT_OZONE_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_OZONE_SELECTED", ImportYesNo.class);
  public static final ParameterisedJourneyEvent<ImportYesNo> IMPORT_DRUGS_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_DRUGS_SELECTED", ImportYesNo.class);
  public static final ParameterisedJourneyEvent<ImportFoodWhat> IMPORT_FOOD_WHAT_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_FOOD_WHAT_SELECTED", ImportFoodWhat.class);
  public static final ParameterisedJourneyEvent<ImportYesNo> IMPORT_ENDANGERED_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_ENDANGERED_SELECTED", ImportYesNo.class);

  public static final ParameterisedJourneyEvent<ImportWhatWhereIron> IMPORT_WHAT_WHERE_IRON_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_WHAT_WHERE_IRON_SELECTED", ImportWhatWhereIron.class);
  public static final ParameterisedJourneyEvent<ImportWhatWhereTextiles> IMPORT_WHAT_WHERE_TEXTILES_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_WHAT_WHERE_TEXTILES_SELECTED", ImportWhatWhereTextiles.class);
  public static final ParameterisedJourneyEvent<ImportYesNo> IMPORT_BELARUS_TEXTILES_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_BELARUS_TEXTILES_SELECTED", ImportYesNo.class);

}
