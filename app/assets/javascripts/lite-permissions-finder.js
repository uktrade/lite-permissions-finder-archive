var LITEPermissionsFinder = LITEPermissionsFinder || {};

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
  }
};

LITEPermissionsFinder.DestinationCountry = {  
  setupPage: function() {
    LITEPermissionsFinder.countrySetup.setup();

    $("#through-destination-countries-wrapper").hide();

    $("#itemThroughMultipleCountries-T").change(function() {
      if ($(this).is(":checked")) {
        $("#through-destination-countries-wrapper").show();
      }
    }).trigger("change");

    $("#itemThroughMultipleCountries-F").change(function() {
      if ($(this).is(":checked")) {
        $("#through-destination-countries-wrapper").hide();
      }
    });
  }
};

LITEPermissionsFinder.ImportCountry = {
  setupPage: function() {
    LITEPermissionsFinder.countrySetup.setup();
  }
};

LITEPermissionsFinder.countrySetup = {
  setup: function() {
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

LITEPermissionsFinder.Search = {
  setupPage: function() {
    LITEPermissionsFinder.Search._bindAnalyticsTriggers();
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

LITEPermissionsFinder.SearchResults = {
  setupPage: function() {
    var showMoreResultsButton = LITEPermissionsFinder.SearchResults._showMoreResultsButton();
    showMoreResultsButton.click(LITEPermissionsFinder.SearchResults._showMoreResults);
    // Focus the last chosen control code (triggered using the back link)
    var lastChosenControlCode = $('#lastChosenControlCodeHiddenInput').val();
    if (lastChosenControlCode !== null && lastChosenControlCode != "undefined") {
      $('#' + lastChosenControlCode + '-button').focus();
    }
  },
  onload: function() {
    var resultsDisplayCountHiddenInput = LITEPermissionsFinder.SearchResults._resultsDisplayCountHiddenInput();
    var resultsDisplayCount = parseInt(resultsDisplayCountHiddenInput.val());
    var currentResultsCount = parseInt(LITEPermissionsFinder.SearchResults._currentResultsCount());
    if (resultsDisplayCount != currentResultsCount) {
      resultsDisplayCountHiddenInput.val(currentResultsCount);
      var fromIndex = currentResultsCount;
      var toIndex = resultsDisplayCount;
      LITEPermissionsFinder.SearchResults._fetchResults(fromIndex, toIndex);
    }
  },
  _showMoreResults: function() {
    var paginationSize = parseInt(LITEPermissionsFinder.SearchResults._paginationSizeHiddenInput().val());
    var currentResultsCount = parseInt(LITEPermissionsFinder.SearchResults._currentResultsCount());
    var fromIndex = currentResultsCount;
    var toIndex = currentResultsCount + paginationSize;
    LITEPermissionsFinder.SearchResults._fetchResults(fromIndex, toIndex);
  },
  _fetchResults: function(fromIndex, toIndex) {
    var transactionId = $("input[type=hidden][name=ctx_transaction]").val();
    var controlCodeSubJourney = LITEPermissionsFinder.SearchResults._controlCodeSubJourneyHiddenInput().val();
    var showMoreResultsButton = LITEPermissionsFinder.SearchResults._showMoreResultsButton();
    var resultsDisplayCountHiddenInput = LITEPermissionsFinder.SearchResults._resultsDisplayCountHiddenInput();
    var route = jsRoutes.controllers.search.AjaxSearchResultsController.getResults(controlCodeSubJourney, fromIndex, toIndex, transactionId);
    $.ajax({
      url: route.url,
      type: route.type,
      beforeSend : function() {
        showMoreResultsButton.attr("disabled", true);
      },
      success : function(response) {
        if (typeof response != "undefined" && response.status == "ok") {
          $.each(response.results, function (index, result) {
          $("#searchResults").append(
              $("<li/>").append(
                $("<button/>")
                  .attr("id", result.controlCode + "-button")
                  .attr("type", "submit")
                  .attr("name", "result")
                  .attr("value", result.controlCode)
                  .addClass("link font-medium")
                  .text(result.displayText)
              )
            );
          });
          resultsDisplayCountHiddenInput.val(LITEPermissionsFinder.SearchResults._currentResultsCount());
          if(!response.moreResults) {
            showMoreResultsButton.hide();
          }
        }
      },
      complete : function() {
        showMoreResultsButton.removeAttr("disabled");
      }
    });
  },
  _currentResultsCount: function() {
    return $("#searchResults").children("li").length;
  },
  _resultsDisplayCountHiddenInput: function() {
    return $("#resultsDisplayCountHiddenInput");
  },
  _controlCodeSubJourneyHiddenInput: function() {
    return $("#controlCodeSubJourneyHiddenInput");
  },
  _paginationSizeHiddenInput: function() {
    return $("#paginationSizeHiddenInput");
  },
  _showMoreResultsButton: function() {
    return $("#showMoreResultsButton");
  }
};

LITEPermissionsFinder.SearchRelatedCodes = {
  setupPage: function() {
    var showMoreRelatedCodesButton = LITEPermissionsFinder.SearchRelatedCodes._showMoreRelatedCodesButton();
    showMoreRelatedCodesButton.click(LITEPermissionsFinder.SearchRelatedCodes._showMoreRelatedCodes);
    // Focus the last chosen control code (triggered using the back link)
    var lastChosenRelatedCode = $('#lastChosenRelatedCodeHiddenInput').val();
    if (lastChosenRelatedCode !== null && lastChosenRelatedCode != "undefined") {
      $('#' + lastChosenRelatedCode + '-button').focus();
    }
  },
  onload: function() {
    var relatedCodesDisplayCountHiddenInput = LITEPermissionsFinder.SearchRelatedCodes._relatedCodesDisplayCountHiddenInput();
    var relatedCodesDisplayCount = parseInt(relatedCodesDisplayCountHiddenInput.val());
    var currentRelatedCodesCount = parseInt(LITEPermissionsFinder.SearchRelatedCodes._currentRelatedCodesCount());
    if (relatedCodesDisplayCount != currentRelatedCodesCount) {
      relatedCodesDisplayCountHiddenInput.val(currentRelatedCodesCount);
      var fromIndex = currentRelatedCodesCount;
      var toIndex = relatedCodesDisplayCount;
      LITEPermissionsFinder.SearchRelatedCodes._fetchRelatedCodes(fromIndex, toIndex);
    }
  },
  _showMoreRelatedCodes: function() {
    var paginationSize = parseInt(LITEPermissionsFinder.SearchRelatedCodes._paginationSizeHiddenInput().val());
    var currentRelatedCodesCount = parseInt(LITEPermissionsFinder.SearchRelatedCodes._currentRelatedCodesCount());
    var fromIndex = currentRelatedCodesCount;
    var toIndex = currentRelatedCodesCount + paginationSize;
    LITEPermissionsFinder.SearchRelatedCodes._fetchRelatedCodes(fromIndex, toIndex);
  },
  _fetchRelatedCodes: function(fromIndex, toIndex) {
    var transactionId = $("input[type=hidden][name=ctx_transaction]").val();
    var controlCodeSubJourney = LITEPermissionsFinder.SearchRelatedCodes._controlCodeSubJourneyHiddenInput().val();
    var showMoreRelatedCodesButton = LITEPermissionsFinder.SearchRelatedCodes._showMoreRelatedCodesButton();
    var relatedCodesDisplayCountHiddenInput = LITEPermissionsFinder.SearchRelatedCodes._relatedCodesDisplayCountHiddenInput();
    var route = jsRoutes.controllers.search.AjaxSearchRelatedCodesController.getRelatedCodes(controlCodeSubJourney, fromIndex, toIndex, transactionId);
    $.ajax({
      url: route.url,
      type: route.type,
      beforeSend : function() {
        showMoreRelatedCodesButton.attr("disabled", true);
      },
      success : function(response) {
        if (typeof response != "undefined" && response.status == "ok") {
          $.each(response.relatedCodes, function (index, relatedCode) {
            $("#searchRelatedCodes").append(
              $("<li/>").append(
                $("<button/>")
                  .attr("id", relatedCode.controlCode + "-button")
                  .attr("type", "submit")
                  .attr("name", "relatedCode")
                  .attr("value", relatedCode.controlCode)
                  .addClass("link font-medium")
                  .text(relatedCode.displayText)
              )
            );
          });
          relatedCodesDisplayCountHiddenInput.val(LITEPermissionsFinder.SearchRelatedCodes._currentRelatedCodesCount());
          if(!response.moreRelatedCodes) {
            showMoreRelatedCodesButton.hide();
          }
        }
      },
      complete : function() {
        showMoreRelatedCodesButton.removeAttr("disabled");
      }
    });
  },
  _currentRelatedCodesCount: function() {
    return $("#searchRelatedCodes").children("li").length;
  },
  _relatedCodesDisplayCountHiddenInput: function() {
    return $("#relatedCodesDisplayCountHiddenInput");
  },
  _controlCodeSubJourneyHiddenInput: function() {
    return $("#controlCodeSubJourneyHiddenInput");
  },
  _paginationSizeHiddenInput: function() {
    return $("#paginationSizeHiddenInput");
  },
  _showMoreRelatedCodesButton: function() {
    return $("#showMoreRelatedCodesButton");
  }
};

LITEPermissionsFinder.ExportCategories = {
  setupPage: function() {
    LITEPermissionsFinder.ExportCategories._bindAnalyticsTriggers();
  },
  _bindAnalyticsTriggers: function() {
    $("button[name='category']").parents('details').children('summary').click(function () {
      if (!$(this).parent("details").is("[open]")) {
        var category = $(this).siblings().find("button[name='category']").first().val();
        _paq.push(['trackEvent', 'exportCategories', 'openedCategory', category]);
      }
    });
  }
};

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

LITEPermissionsFinder.StartApplication = {
  setupPage: function() {
    LITEPermissionsFinder.StartApplication._bindAnalyticsTriggers();
  },
  _bindAnalyticsTriggers: function() {
    var form = $('form:last');
    form.submit(function (event) {
      var emailAddress = $("input[name='emailAddress']", $('form:last')).val();
      if (typeof emailAddress != "undefined" && emailAddress !== "") {
        _paq.push(['trackEvent', 'emailAddress', 'emailAddress', emailAddress]);
      }
    });
  }
};

$(document).ready(LITEPermissionsFinder.Utils.ready);

$(window).load(LITEPermissionsFinder.Utils.load);