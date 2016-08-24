package controllers;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.mvc.Result;
import views.html.startApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class StartApplicationController {

  private static final List<Character> CODE_DIGITS = Collections.unmodifiableList(Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));

  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;
  private final TradeTypeController tradeTypeController;

  @Inject
  public StartApplicationController(FormFactory formFactory, PermissionsFinderDao dao, TradeTypeController tradeTypeController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.tradeTypeController = tradeTypeController;
  }

  public Result renderForm() {
    String applicationCode = generateApplicationCode();
    dao.saveApplicationCode(applicationCode);
    return ok(startApplication.render(formFactory.form(StartApplicationForm.class), applicationCode));
  }

  public Result handleSubmit() {
    Form<StartApplicationForm> form = formFactory.form(StartApplicationForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return ok(startApplication.render(form, dao.getApplicationCode()));
    }
    String emailAddress = form.get().emailAddress;
    String memorableWord = form.get().memorableWord;
    if (emailAddress != null && !emailAddress.isEmpty()) {
      dao.saveEmailAddress(emailAddress);
    }
    if (memorableWord != null && !memorableWord.isEmpty()) {
      dao.saveMemorableWord(memorableWord);
      return tradeTypeController.renderForm();
    }
    return badRequest("Unhandled form state");
  }

  /**
   * Builds a random application code satisfying the regular expression \[0-9A-Z]{4}[\-][0-9A-Z]{4}\
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

    @Required(message = "You must enter a memorable word")
    public String memorableWord;

    public List<ValidationError> validate() {
      List<ValidationError> errors = new ArrayList<>();
      if (memorableWord != null && memorableWord.trim().length() <= 3) {
        errors.add(new ValidationError("memorableWord", "Please make your word at least three letters in length"));
      }
      return errors.isEmpty() ? null : errors;
    }

  }
}
