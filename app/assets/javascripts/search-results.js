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
