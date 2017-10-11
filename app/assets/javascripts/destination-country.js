var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.DestinationCountry = {
  setupPage: function() {
    $("select[data-ui-autocomplete='ui-autocomplete']").each(function() {
      LITECommon.countrySelectInitialise($(this));
    });

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
