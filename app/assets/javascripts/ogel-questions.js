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
      var before1897upto35k = boolToYesNo($("input[name='before1897upto35k']:checked", form).val());
      _paq.push(['trackEvent', 'ogelQuestions', 'forRepairReplacement', forRepairReplacement]);
      _paq.push(['trackEvent', 'ogelQuestions', 'forExhibitionDemonstration', forExhibitionDemonstration]);
      _paq.push(['trackEvent', 'ogelQuestions', 'before1897upto35k', before1897upto35k]);
    });

  }
};
