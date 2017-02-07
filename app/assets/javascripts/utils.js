LITEPermissionsFinder.Utils = {
  ready: function() {
    var pageName = LITEPermissionsFinder.Utils.getPageName();
    if (pageName !== null && pageName != "undefined") {
      if (pageName == "destinationCountry") {
        LITEPermissionsFinder.DestinationCountry.setupPage();
      }
      if (pageName == "importCountry") {
        LITEPermissionsFinder.ImportCountry.setupPage();
      }
      else if (pageName == "search") {
        LITEPermissionsFinder.Search.setupPage();
      }
      else if (pageName == "searchResults") {
        LITEPermissionsFinder.SearchResults.setupPage();
      }
      else if (pageName == "searchRelatedCodes") {
        LITEPermissionsFinder.SearchRelatedCodes.setupPage();
      }
      else if (pageName == "selectExportCategories") {
        LITEPermissionsFinder.ExportCategories.setupPage();
      }
      else if (pageName == "ogelQuestions") {
        LITEPermissionsFinder.OgelQuestions.setupPage();
      }
      else if (pageName == "startApplication") {
        LITEPermissionsFinder.StartApplication.setupPage();
      }
    }
  },
  load: function() {
    var pageName = LITEPermissionsFinder.Utils.getPageName();
    if (pageName !== null && pageName != "undefined") {
      if (pageName == "searchResults") {
        LITEPermissionsFinder.SearchResults.onload();
      }
      else if (pageName == "searchRelatedCodes") {
        LITEPermissionsFinder.SearchRelatedCodes.onload();
      }
    }
  },
  getPageName: function() {
    return $("meta[property='LITEPermissionsFinder:pageName']").attr("content");
  },
  countrySetup: function() {
    $("select[ui-autocomplete='ui-autocomplete']").selectToAutocomplete({"alternative-spellings-attr":"data-alternative-spelling", "autoFocus":false});

    // Associates the new ui-autocomplete input with the original select id (if the input was created), needed for labels and such.
    $("select[ui-autocomplete='ui-autocomplete']").each(function() {
      var id = $(this).attr("id");
      var autocompleteInput = $("input[ui-autocomplete-id=" + id + "]");
      if (autocompleteInput.length > 0) {
        autocompleteInput.attr("id", id);
        $(this).removeAttr("id");
      }
    });
  }
};
