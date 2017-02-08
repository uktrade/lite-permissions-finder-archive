var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.DestinationCountry = {
  setupPage: function() {
    LITEPermissionsFinder.Utils.countrySetup();

    $("#itemThroughMultipleCountries-T").change(function() {
      if ($(this).is(":checked")) {
        LITECommon.showContent($("#through-destination-countries-wrapper"));
      }
    }).trigger("change");

    $("#itemThroughMultipleCountries-F").change(function() {
      if ($(this).is(":checked")) {
        LITECommon.hideContent($("#through-destination-countries-wrapper"));
      }
    }).trigger("change");
  }
};
