$(document).ready(function (){
  $("[ui-autocomplete='ui-autocomplete']").selectToAutocomplete({"alternative-spellings-attr":"data-alternative-spelling", autoFocus:false});

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

});