package controllers;


import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import components.persistence.LicenceFinderDao;
import models.persistence.RegisterLicence;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Result;

public class LicenceFinderPollController {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LicenceFinderPollController.class);

  private final LicenceFinderDao licenceFinderDao;

  @Inject
  public LicenceFinderPollController(LicenceFinderDao licenceFinderDao) {
    this.licenceFinderDao = licenceFinderDao;
  }

  /***
   * Polls to check any registration reference for current transaction
   */
  public Result pollStatus(String sessionId) {
    ObjectNode json = Json.newObject();
    try {
      boolean complete = licenceFinderDao.getRegisterLicence(sessionId)
          .map(RegisterLicence::getRegistrationReference)
          .isPresent();
      json.put("complete", complete);
    } catch (Exception e) {
      LOGGER.error("Error reading registration submission status for " + sessionId, e);
      json.put("complete", false);
    }
    return ok(json);
  }
}