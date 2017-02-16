package importcontent;

import components.common.journey.ParameterisedJourneyEvent;
import importcontent.models.ImportFoodWhat;
import importcontent.models.ImportWhat;
import importcontent.models.ImportWhere;

public class ImportEvents {
  public static final ParameterisedJourneyEvent<ImportWhat> IMPORT_WHAT_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_WHAT_SELECTED", ImportWhat.class);
  public static final ParameterisedJourneyEvent<ImportWhere> IMPORT_WHERE_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_WHERE_SELECTED", ImportWhere.class);
  public static final ParameterisedJourneyEvent<ImportFoodWhat> IMPORT_FOOD_WHAT_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_FOOD_WHAT_SELECTED", ImportFoodWhat.class);
  public static final ParameterisedJourneyEvent<Boolean> IMPORT_YES_NO_SELECTED = new ParameterisedJourneyEvent<>("IMPORT_YES_NO_SELECTED", Boolean.class);
}
