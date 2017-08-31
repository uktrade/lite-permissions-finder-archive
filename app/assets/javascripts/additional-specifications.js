var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.AdditionalSpecifications = {
  setupPage: function() {

    $("#stillDescribesItems-T").change(function() {
      if ($(this).is(":checked")) {
        LITECommon.showContent($("#showTechNotes-wrapper"));
        $("#showTechNotes").data("validation").required = {"message" : "Select whether you want to read technical information about this item"};
      }
    }).trigger("change");

    $("#stillDescribesItems-F").change(function() {
      if ($(this).is(":checked")) {
        LITECommon.hideContent($("#showTechNotes-wrapper"));
        $("#showTechNotes").data("validation", {});
      }
    }).trigger("change");

  }
};
