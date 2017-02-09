var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.AdditionalSpecifications = {
  setupPage: function() {

    $("#stillDescribesItems-T").change(function() {
      if ($(this).is(":checked")) {
        LITECommon.showContent($("#showTechNotes-wrapper"));
        $("#showTechNotes").attr("data-validation", '{"required":{"message":"You must answer this question"}}');
      }
    }).trigger("change");

    $("#stillDescribesItems-F").change(function() {
      if ($(this).is(":checked")) {
        LITECommon.hideContent($("#showTechNotes-wrapper"));
        $("#showTechNotes").removeAttr("data-validation");
      }
    }).trigger("change");

  }
};
