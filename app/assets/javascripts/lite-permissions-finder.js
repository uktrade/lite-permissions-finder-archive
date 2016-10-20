$(document).ready(function (){
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

  $("#itemThroughMultipleCountries-T").change(function (){
    if ($(this).is(":checked")) {
      $("#through-destination-countries-wrapper").show();
    }
  }).trigger("change");

  $("#itemThroughMultipleCountries-F").change(function (){
    if ($(this).is(":checked")) {
      $("#through-destination-countries-wrapper").hide();
    }
  });

  var showMoreResultsButton = $("#showMoreResultsButton");

  var searchResults = $("#searchResults");

  showMoreResultsButton.click(function (e) {
    var transactionId = $("input[type=hidden][name=ctx_transaction]").val();
    var goodsType = $("#goodsTypeHiddenInput").val();
    var resultsDisplayCountHiddenInput = $("#resultsDisplayCountHiddenInput");
    var route = jsRoutes.controllers.search.AjaxSearchResultsController.getResults(goodsType, resultsDisplayCountHiddenInput.val(), transactionId);
    $.ajax({
      url: route.url,
      type: route.type,
      beforeSend : function () {
        showMoreResultsButton.attr("disabled", true);
      },
      success : function (response) {
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
          resultsDisplayCountHiddenInput.val($("#searchResults").children("tbody").children("tr").length);
          if(!response.moreResults) {
            $('#showMoreResultsWrapper').hide();
          }
        }
      },
      complete : function () {
        showMoreResultsButton.removeAttr("disabled");
      }
    });
  });
});