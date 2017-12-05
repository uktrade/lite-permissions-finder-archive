package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.state.ContextParamManager;
import components.common.transaction.TransactionManager;
import components.persistence.ApplicationCodeDao;
import components.persistence.PermissionsFinderDao;
import components.services.notification.PermissionsFinderNotificationClient;
import journey.JourneyDefinitionNames;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Email;
import play.mvc.Result;
import utils.appcode.ApplicationCodeContextParamProvider;
import views.html.startApplication;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class StartApplicationController {

  private static final List<Character> CODE_DIGITS = Collections.unmodifiableList(Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'));

  private final TransactionManager transactionManager;
  private final ContextParamManager contextParamManager;
  private final FormFactory formFactory;
  private final ApplicationCodeDao applicationCodeDao;
  private final PermissionsFinderDao permissionsFinderDao;
  private final PermissionsFinderNotificationClient notificationClient;
  private final ApplicationCodeContextParamProvider applicationCodeContextParamProvider;
  private final JourneyManager journeyManager;

  @Inject
  public StartApplicationController(TransactionManager transactionManager,
                                    ContextParamManager contextParamManager,
                                    FormFactory formFactory,
                                    PermissionsFinderDao permissionsFinderDao,
                                    ApplicationCodeDao applicationCodeDao,
                                    PermissionsFinderNotificationClient notificationClient,
                                    ApplicationCodeContextParamProvider applicationCodeContextParamProvider,
                                    JourneyManager journeyManager) {
    this.transactionManager = transactionManager;
    this.contextParamManager = contextParamManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.applicationCodeDao = applicationCodeDao;
    this.notificationClient = notificationClient;
    this.applicationCodeContextParamProvider = applicationCodeContextParamProvider;
    this.journeyManager = journeyManager;
  }

  public Result renderForm() {
    // Only set transaction ID if not already set
    if (transactionManager.isTransactionIdAvailable()){
      transactionManager.getTransactionId();
    }
    else {
      transactionManager.createTransaction();
    }

    String applicationCode = permissionsFinderDao.getApplicationCode();
    if (applicationCode == null || applicationCode.isEmpty()) {
      applicationCode = generateApplicationCode();
      applicationCodeDao.writeTransactionId(applicationCode);
      permissionsFinderDao.saveApplicationCode(applicationCode);
    }

    applicationCodeContextParamProvider.updateParamValueOnContext(applicationCode);

    return ok(startApplication.render(formFactory.form(StartApplicationForm.class), applicationCode));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<StartApplicationForm> form = formFactory.form(StartApplicationForm.class).bindFromRequest();
    String applicationCode = permissionsFinderDao.getApplicationCode();
    if (form.hasErrors()) {
      return completedFuture(ok(startApplication.render(form, applicationCode)));
    }

    String emailAddress = form.get().emailAddress;

    if (StringUtils.isNoneBlank(emailAddress)) {
      permissionsFinderDao.saveEmailAddress(emailAddress.trim());
      notificationClient.sendApplicationReferenceEmail(emailAddress.trim(), applicationCode);
    }

    applicationCodeContextParamProvider.updateParamValueOnContext(applicationCode);
    return journeyManager.startJourney(JourneyDefinitionNames.EXPORT);
  }

  /**
   * Builds a random application code satisfying the regular expression \[0-9A-Z]{4}[\-][0-9A-Z]{4}\ and Crockford encoding compliant
   * e.g. XME1-BM7S
   * @return The application code
   */
  public String generateApplicationCode() {
    StringBuilder sb = new StringBuilder();
    IntStream.range(0,8).forEach(i -> sb.append(CODE_DIGITS.get(ThreadLocalRandom.current().nextInt(0, CODE_DIGITS.size()))));
    return sb.insert(4,"-").toString();
  }

  public static class StartApplicationForm {

    @Email()
    public String emailAddress;

  }
}
