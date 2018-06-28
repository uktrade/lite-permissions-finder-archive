var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.DestinationCountry = {
  setupPage: function() {
    $("select[data-ui-autocomplete='ui-autocomplete']").each(function() {
      LITECommon.countrySelectInitialise($(this));
    });
  }
};

$(document).ready(function(){
  LITEPermissionsFinder.DestinationCountry.setupPage();
});
