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
      else if (pageName == "searchBase") {
        LITEPermissionsFinder.Search.setupPage();
      }
      else if (pageName == "searchResultsBase") {
        LITEPermissionsFinder.SearchResults.setupPage();
      }
      else if (pageName == "selectExportCategories") {
        LITEPermissionsFinder.ExportCategories.setupPage();
      }
      else if (pageName == "ogelQuestions") {
        LITEPermissionsFinder.OgelQuestions.setupPage();
      }
    }
  },
  load: function() {
    var pageName = LITEPermissionsFinder.Utils.getPageName();
    if (pageName !== null && pageName != "undefined") {
      if (pageName == "searchResultsBase") {
        LITEPermissionsFinder.SearchResults.onload();
      }
    }
  },
  getPageName: function() {
    return $("meta[property='LITEPermissionsFinder:pageName']").attr("content");
  }
};

LITEPermissionsFinder.DestinationCountry = {
  setupPage: function() {
    LITEPermissionsFinder.countrySetup._setup();

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
    LITEPermissionsFinder.countrySetup._setup();
  }
};

LITEPermissionsFinder.countrySetup = {
  _setup: function() {
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
      var inputs = $(this).find("input[type='text']");
      var description = textareas.filter("[name='description']").val();
      var component = textareas.filter("[name='component']").val();
      var brand = inputs.filter("[name='brand']").val();
      var partNumber = inputs.filter("[name='partNumber']").val();
      var eventValue = "description: " + description + ";" +
        " component: " + component + ";" +
        " brand: " + brand + ";" +
        " partNumber: " + partNumber + ";";

      _paq.push(['trackSiteSearch', eventValue, "physical", false]);
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
    var goodsType = LITEPermissionsFinder.SearchResults._goodsTypeHiddenInput().val();
    var controlCodeJourney = LITEPermissionsFinder.SearchResults._controlCodeJourneyHiddenInput().val();
    var showMoreResultsButton = LITEPermissionsFinder.SearchResults._showMoreResultsButton();
    var resultsDisplayCountHiddenInput = LITEPermissionsFinder.SearchResults._resultsDisplayCountHiddenInput();
    var route = jsRoutes.controllers.search.AjaxSearchResultsController.getResults(controlCodeJourney, goodsType, fromIndex, toIndex, transactionId);
    $.ajax({
      url: route.url,
      type: route.type,
      beforeSend : function() {
        showMoreResultsButton.attr("disabled", true);
      },
      success : function(response) {
        if (typeof response != "undefined" && response.status == "ok") {
          $.each(response.results, function (index, result) {
            $("#searchResults").children("tbody").append(
              $("<tr/>").append(
                $("<td/>").append(
                  $("<button/>")
                    .attr("id", result.code + "-button")
                    .attr("type", "submit")
                    .attr("name", "result")
                    .attr("value", result.code)
                    .addClass("link font-medium")
                    .text(result.highlightedText)
                )
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
    return $("#searchResults").children("tbody").children("tr").length;
  },
  _resultsDisplayCountHiddenInput: function() {
    return $("#resultsDisplayCountHiddenInput");
  },
  _controlCodeJourneyHiddenInput: function() {
    return $("#controlCodeJourneyHiddenInput");
  },
  _goodsTypeHiddenInput: function() {
    return $("#goodsTypeHiddenInput");
  },
  _paginationSizeHiddenInput: function() {
    return $("#paginationSizeHiddenInput");
  },
  _showMoreResultsButton: function() {
    return $("#showMoreResultsButton");
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

$(document).ready(LITEPermissionsFinder.Utils.ready);

$(window).load(LITEPermissionsFinder.Utils.load);