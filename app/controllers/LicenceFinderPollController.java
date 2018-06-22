package controllers;


import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import components.services.LicenceFinderService;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;

public class LicenceFinderPollController {

  private final LicenceFinderService licenceFinderService;

  @Inject
  public LicenceFinderPollController(LicenceFinderService licenceFinderService) {
    this.licenceFinderService = licenceFinderService;
  }

  /***
   * Polls to check any registration reference for current transaction
   */
  public Result pollStatus(String sessionId) {
    ObjectNode json = Json.newObject();
    try {
      json.put("complete", licenceFinderService.getRegistrationReference(sessionId).isPresent());
    } catch (Exception e) {
      Logger.error("Error reading registration submission status for " + sessionId, e);
      json.put("complete", false);
    }
    return ok(json);
  }
}