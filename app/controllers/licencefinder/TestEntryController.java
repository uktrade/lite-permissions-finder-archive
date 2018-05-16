package controllers.licencefinder;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.transaction.TransactionManager;
import components.persistence.PermissionsFinderDao;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

public class TestEntryController extends Controller {

  public static final String JOURNEY_NAME = "TEST_CONTROL_ENTRY";
  private final TransactionManager transactionManager;
  private final JourneyManager journeyManager;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public TestEntryController(TransactionManager transactionManager,
                             JourneyManager journeyManager,
                             PermissionsFinderDao permissionsFinderDao) {
    this.transactionManager = transactionManager;
    this.journeyManager = journeyManager;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public CompletionStage<Result> testEntry(String controlCode) {

    transactionManager.createTransaction();

    permissionsFinderDao.saveControlCodeForRegistration(controlCode);
    permissionsFinderDao.saveApplicationCode("ABCD-1234");

    return journeyManager.startJourney(JOURNEY_NAME);
  }

}
