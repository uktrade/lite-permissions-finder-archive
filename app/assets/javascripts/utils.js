LITEPermissionsFinder.Utils = {
  ready: function() {
    // Triggered on $(document).ready()
  },
  load: function() {
    // Triggered on $(window).load()
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
