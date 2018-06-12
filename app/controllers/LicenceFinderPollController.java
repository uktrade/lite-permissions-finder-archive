package controllers;


import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import components.common.CommonContextAction;
import components.services.LicenceFinderService;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;

@With(CommonContextAction.class)
public class LicenceFinderPollController {

  private final LicenceFinderService licenceFinderService;

  @Inject
  public LicenceFinderPollController(LicenceFinderService licenceFinderService) {
    this.licenceFinderService = licenceFinderService;
  }

  /***
   * Polls to check any registration reference for current transaction
   */
  public Result pollStatus(String transactionId) {
    ObjectNode json = Json.newObject();
    try {
      json.put("complete", licenceFinderService.getRegistrationReference(transactionId).isPresent());
    } catch (Exception e) {
      Logger.error("Error reading registration submission status for " + transactionId, e);
      json.put("complete", false);
    }
    return ok(json);
  }
}