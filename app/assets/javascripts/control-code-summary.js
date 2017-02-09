var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.ControlCodeSummary = {
  setupPage: function() {

    $("#couldDescribeItems-T").change(function() {
      if ($(this).is(":checked")) {
        LITECommon.showContent($("#showTechNotes-wrapper"));
        $("#showTechNotes").attr("data-validation", '{"required":{"message":"You must answer this question"}}');
      }
    }).trigger("change");

    $("#couldDescribeItems-F").change(function() {
      if ($(this).is(":checked")) {
        LITECommon.hideContent($("#showTechNotes-wrapper"));
        $("#showTechNotes").removeAttr("data-validation");
      }
    }).trigger("change");

  }
};
