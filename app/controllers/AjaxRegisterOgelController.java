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

import java.util.Optional;

@With(CommonContextAction.class)
public class AjaxRegisterOgelController {

  private final LicenceFinderService licenceFinderService;

  @Inject
  public AjaxRegisterOgelController(LicenceFinderService licenceFinderService) {
    this.licenceFinderService = licenceFinderService;
  }

  /***
   * Polls to check any registration reference for current transaction
   */
  public Result pollStatus(String transactionId) {
    ObjectNode json = Json.newObject();
    try {
      Optional<String> regRef = licenceFinderService.getRegistrationReference(transactionId);
      if (regRef.isPresent()) {
        json.put("complete", true);
      } else {
        json.put("complete", false);
      }
    } catch (Exception e) {
      Logger.error("Error reading registration submission status for " + transactionId, e);
      json.put("complete", false);
    }
    Logger.info("pollStatus: " + json);
    return ok(json);
  }

}