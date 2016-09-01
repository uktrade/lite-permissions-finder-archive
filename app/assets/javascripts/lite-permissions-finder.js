$(document).ready(function (){
  $("[id^='destinationCountry']").selectToAutocomplete({"alternative-spellings-attr":"data-alternative-spelling", autoFocus:false});
});