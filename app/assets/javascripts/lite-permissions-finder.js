var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.Utils = {
  ready: function() {
    var pageName = LITEPermissionsFinder.Utils.getPageName();
    if (pageName !== null && pageName != "undefined") {
      if (pageName == "destinationCountry") {
        LITEPermissionsFinder.DestinationCountry.setupPage();
      }
      else if (pageName == "searchResultsBase") {
        LITEPermissionsFinder.SearchResults.setupPage();
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

LITEPermissionsFinder.SearchResults = {
  setupPage: function() {
    var showMoreResultsButton = LITEPermissionsFinder.SearchResults._showMoreResultsButton();
    showMoreResultsButton.click(LITEPermissionsFinder.SearchResults._showMoreResults);
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
    var showMoreResultsButton = LITEPermissionsFinder.SearchResults._showMoreResultsButton();
    var resultsDisplayCountHiddenInput = LITEPermissionsFinder.SearchResults._resultsDisplayCountHiddenInput();
    var route = jsRoutes.controllers.search.AjaxSearchResultsController.getResults(goodsType, fromIndex, toIndex, transactionId);
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
                  $("<p/>").append(
                    $("<button/>")
                      .attr("type", "submit")
                      .attr("name", "result")
                      .attr("value", result.code)
                      .addClass("link font-xsmall")
                      .text(result.highlightedText)
                  )
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

$(document).ready(LITEPermissionsFinder.Utils.ready);

$(window).load(LITEPermissionsFinder.Utils.load);