var LITEOgelRegistration = {};

/**
 * OGEL Registration Interval
 */
LITEOgelRegistration.RegistrationInterval = {

  setupPage: function () {
    var route = jsRoutes.controllers.LicenceFinderPollController.pollStatus($("input[name=ctx_transaction]").val());
    LITEOgelRegistration.RegistrationInterval._pollRegistrationStatus(route);
    setTimeout(function(){
      $('#pleaseWaitHeading').addClass('hidden');
      $('#longRegistrationMessage').removeClass('hidden');
    }, 15000);
  },

  /**
   * Polls the current OGEL Registration status via an AJAX call
   *
   * @param route the endpoint to call
   * @private
   */
  _pollRegistrationStatus: function pollRegistrationStatus(route) {
    setTimeout(function () {
      var registrationCompleted;
      $.ajax({
        url: route.url,
        type: route.type,
        success: function (data) {
          registrationCompleted = data.complete;
        },
        error: function (jqXHR, textStatus, errorThrown) {
          console.log("Poll registration submission status error - " + errorThrown);
        },
        complete: function () {
          if (registrationCompleted === true) {
            $("#registrationInterval").submit();
          } else {
            pollRegistrationStatus(route);
          }
        },
        timeout: 2000
      });
    }, 3000);
  }
};
