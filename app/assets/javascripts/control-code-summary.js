var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.ControlCodeSummary = {
  setupPage: function() {

    $("#couldDescribeItems-T").change(function() {
      if ($(this).is(":checked")) {
        LITECommon.showContent($("#showTechNotes-wrapper"));
        $("#showTechNotes").data("validation").required = {"message" : "Answer this question"};
      }
    }).trigger("change");

    $("#couldDescribeItems-F").change(function() {
      if ($(this).is(":checked")) {
        LITECommon.hideContent($("#showTechNotes-wrapper"));
        $("#showTechNotes").data("validation", {});
      }
    }).trigger("change");

  }
};
