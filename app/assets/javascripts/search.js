var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.Search = {
  setupPage: function() {
    LITEPermissionsFinder.Search._bindAnalyticsTriggers();
    $("#isComponent-T").change(function() {
      if ($(this).is(":checked")) {
        $("#component").data("validation").required = {"message" : "Enter a description of the equipment or system"};
      }
    }).trigger("change");

    $("#isComponent-F").change(function() {
      if ($(this).is(":checked")) {
        $("#component" ).data("validation", {});
      }
    }).trigger("change");
  },
  _bindAnalyticsTriggers: function() {
    $('form:last').submit(function(event) {
      var textareas = $(this).find("textarea");
      var description = textareas.filter("[name='description']").val();
      var component = textareas.filter("[name='component']").val();
      var eventValue = "description: " + description + ";" + " component: " + component + ";";
      // Use the sub journey to determine what variant of the search is being used
      var ctxSubJourney = $(this).find("input[name='ctx_sub_journey']").val();
      var searchType = ctxSubJourney.substring(ctxSubJourney.lastIndexOf(':') + 1);
      _paq.push(['trackSiteSearch', eventValue, searchType, false]);
    });
  }
};
