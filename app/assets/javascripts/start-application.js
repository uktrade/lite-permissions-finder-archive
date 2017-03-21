var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.StartApplication = {
  setupPage: function() {
    LITEPermissionsFinder.StartApplication._bindAnalyticsTriggers();
  },
  _bindAnalyticsTriggers: function() {
    var form = $('form:last');
    form.submit(function (event) {
      var emailAddress = $("input[name='emailAddress']", $('form:last')).val();
      if (typeof emailAddress != "undefined" && emailAddress !== "") {
        _paq.push(['trackEvent', 'emailAddress', 'emailAddress', emailAddress]);
      }
    });
  }
};
