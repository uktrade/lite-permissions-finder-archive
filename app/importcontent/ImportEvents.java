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
  public static final ParameterisedJourneyEvent<ImportFoodWhat> IMPORT_FOOD_WHAT_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_FOOD_WHAT_SELECTED", ImportFoodWhat.class);
  public static final ParameterisedJourneyEvent<ImportYesNo> IMPORT_YES_NO_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_YES_NO_SELECTED", ImportYesNo.class);
}
