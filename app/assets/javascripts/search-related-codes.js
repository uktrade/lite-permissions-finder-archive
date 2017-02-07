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
