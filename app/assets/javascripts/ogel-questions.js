var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.OgelQuestions = {
  setupPage: function() {
    LITEPermissionsFinder.OgelQuestions._bindAnalyticsTriggers();
  },
  _bindAnalyticsTriggers: function() {
    function boolToYesNo (bool){
      if (bool == "true") {
        return "yes";
      }
      else if (bool == "false") {
        return "no";
      }
      else {
        return "";
      }
    }
    var form = $('form:last');
    form.submit(function (event) {
      var forRepairReplacement = boolToYesNo($("input[name='forRepairReplacement']:checked", form).val());
      var forExhibitionDemonstration = boolToYesNo($("input[name='forExhibitionDemonstration']:checked", form).val());
      _paq.push(['trackEvent', 'ogelQuestions', 'forRepairReplacement', forRepairReplacement]);
      _paq.push(['trackEvent', 'ogelQuestions', 'forExhibitionDemonstration', forExhibitionDemonstration]);
    });

  }
};
