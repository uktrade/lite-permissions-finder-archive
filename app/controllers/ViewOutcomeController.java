package controllers;

import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.cms.dao.SessionOutcomeDao;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.services.SessionOutcomeService;
import components.services.UserPrivilegeService;
import models.enums.OutcomeType;
import models.view.form.ItemDescriptionForm;
import org.pac4j.play.java.Secure;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import play.twirl.api.Html;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.session.SessionOutcome;
import triage.session.SessionService;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class ViewOutcomeController {

  private final SessionService sessionService;
  private final SessionOutcomeService sessionOutcomeService;
  private final SessionOutcomeDao sessionOutcomeDao;
  private final UserPrivilegeService userPrivilegeService;
  private final SpireAuthManager spireAuthManager;
  private final JourneyConfigService journeyConfigService;
  private final FormFactory formFactory;
  private final views.html.nlr.nlrRegisterSuccess nlrRegisterSuccess;
  private final views.html.nlr.nlrOutcome nlrOutcome;
  private final views.html.triage.listedOutcomeSaved listedOutcomeSaved;
  private final views.html.nlr.nlrItemDescription nlrItemDescription;

  @Inject
  public ViewOutcomeController(SessionService sessionService, SessionOutcomeService sessionOutcomeService,
                               SessionOutcomeDao sessionOutcomeDao, UserPrivilegeService userPrivilegeService,
                               SpireAuthManager spireAuthManager, JourneyConfigService journeyConfigService,
                               FormFactory formFactory, views.html.nlr.nlrRegisterSuccess nlrRegisterSuccess,
                               views.html.nlr.nlrOutcome nlrOutcome,
                               views.html.triage.listedOutcomeSaved listedOutcomeSaved,
                               views.html.nlr.nlrItemDescription nlrItemDescription) {
    this.sessionService = sessionService;
    this.sessionOutcomeService = sessionOutcomeService;
    this.sessionOutcomeDao = sessionOutcomeDao;
    this.userPrivilegeService = userPrivilegeService;
    this.spireAuthManager = spireAuthManager;
    this.journeyConfigService = journeyConfigService;
    this.formFactory = formFactory;
    this.nlrRegisterSuccess = nlrRegisterSuccess;
    this.nlrOutcome = nlrOutcome;
    this.listedOutcomeSaved = listedOutcomeSaved;
    this.nlrItemDescription = nlrItemDescription;
  }

  public Result renderOutcome(String outcomeId) {
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeById(outcomeId);
    if (sessionOutcome == null) {
      return notFound("Unknown outcomeId " + outcomeId);
    } else {
      String userId = spireAuthManager.getAuthInfoFromContext().getId();
      if (userPrivilegeService.canViewOutcome(userId, sessionOutcome)) {
        if (sessionOutcome.getOutcomeType() == OutcomeType.CONTROL_ENTRY_FOUND) {
          String resumeCode = sessionService.getSessionById(sessionOutcome.getSessionId()).getResumeCode();
          return ok(listedOutcomeSaved.render(resumeCode, new Html(sessionOutcome.getOutcomeHtml())));
        } else {
          return ok(nlrOutcome.render(new Html(sessionOutcome.getOutcomeHtml())));
        }
      } else {
        Logger.error("User with userId {} doesn't have privilege to view outcome with outcomeId {} ",
            userId, sessionOutcome.getId());
        return notFound("Unknown outcomeId " + outcomeId);
      }
    }
  }

  public Result registerSuccess(String sessionId) {
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeBySessionId(sessionId);
    if (sessionOutcome != null) {
      String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
      return ok(nlrRegisterSuccess.render(sessionOutcome.getId(), resumeCode));
    } else {
      return notFound("Unknown sessionId or no outcome for sessionId " + sessionId);
    }
  }

  public Result saveListedOutcome(String sessionId, String controlEntryId) {
    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(controlEntryId);
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeBySessionId(sessionId);
    if (sessionOutcome == null) {
      String userId = spireAuthManager.getAuthInfoFromContext().getId();
      sessionOutcomeService.generateItemListedOutcome(userId, sessionId, controlEntryId);
    }
    return redirect(controllers.licencefinder.routes.TradeController.entry(controlEntryConfig.getControlCode()));
  }

  public Result registerNotFoundNlr(String sessionId, String controlEntryId) {
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeBySessionId(sessionId);
    String submitUrl = controllers.routes.ViewOutcomeController.handleRegisterNotFoundNlrSubmit(sessionId, controlEntryId).toString();
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    if (sessionOutcome == null) {
      Form<ItemDescriptionForm> itemDescriptionForm = formFactory.form(ItemDescriptionForm.class);
      return ok(nlrItemDescription.render(itemDescriptionForm, resumeCode, submitUrl));
    } else {
      return redirect(routes.ViewOutcomeController.registerSuccess(sessionId));
    }
  }

  public Result handleRegisterNotFoundNlrSubmit(String sessionId, String controlEntryId) {
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeBySessionId(sessionId);
    String submitUrl = controllers.routes.ViewOutcomeController.handleRegisterNotFoundNlrSubmit(sessionId, controlEntryId).toString();
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    if (sessionOutcome == null) {
      Form<ItemDescriptionForm> form = formFactory.form(ItemDescriptionForm.class).bindFromRequest();
      if (form.hasErrors()) {
        return ok(nlrItemDescription.render(form, resumeCode, submitUrl));
      } else {
        String description = form.get().description.trim();
        if (description.length() < 2 || description.length() > 200) {
          return ok(nlrItemDescription.render(form.withError("description", "Item description is required."),
              resumeCode, submitUrl));
        } else {
          String userId = spireAuthManager.getAuthInfoFromContext().getId();
          sessionOutcomeService.generateNotFoundNlrLetter(userId, sessionId, controlEntryId, resumeCode, description);
          return redirect(routes.ViewOutcomeController.registerSuccess(sessionId));
        }
      }
    } else {
      return redirect(routes.ViewOutcomeController.registerSuccess(sessionId));
    }
  }

  public Result registerDecontrolNlr(String sessionId, String stageId) {
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeBySessionId(sessionId);
    String submitUrl = controllers.routes.ViewOutcomeController.handleRegisterDecontrolNlrSubmit(sessionId, stageId).toString();
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    if (sessionOutcome == null) {
      Form<ItemDescriptionForm> itemDescriptionForm = formFactory.form(ItemDescriptionForm.class);
      return ok(nlrItemDescription.render(itemDescriptionForm, resumeCode, submitUrl));
    } else {
      return redirect(routes.ViewOutcomeController.registerSuccess(sessionId));
    }
  }

  public Result handleRegisterDecontrolNlrSubmit(String sessionId, String stageId) {
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeBySessionId(sessionId);
    String submitUrl = controllers.routes.ViewOutcomeController.handleRegisterDecontrolNlrSubmit(sessionId, stageId).toString();
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    if (sessionOutcome == null) {
      Form<ItemDescriptionForm> form = formFactory.form(ItemDescriptionForm.class).bindFromRequest();
      if (form.hasErrors()) {
        return ok(nlrItemDescription.render(form, resumeCode, submitUrl));
      } else {
        String description = form.get().description.trim();
        if (description.length() < 2) {
          return ok(nlrItemDescription.render(form.withError("description", "Item description is required."),
              resumeCode, submitUrl));
        } else {
          String userId = spireAuthManager.getAuthInfoFromContext().getId();
          sessionOutcomeService.generateDecontrolNlrLetter(userId, sessionId, stageId, resumeCode, description);
          return redirect(routes.ViewOutcomeController.registerSuccess(sessionId));
        }
      }
    } else {
      return redirect(routes.ViewOutcomeController.registerSuccess(sessionId));
    }
  }

}