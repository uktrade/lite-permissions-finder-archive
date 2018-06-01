var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.Triage = {
  setupPage: function() {
    LITEPermissionsFinder.Triage._bindModals($(document.body));
  },
  _bindModals: function($target) {
    $target.find("a[data-control-entry-id], a[data-definition-id]").click(LITEPermissionsFinder.Triage._modalLinkOnClick);
  },
  _modalLinkOnClick: function(event) {
    event.preventDefault();
    var $target = $(event.target);
    var dataControlEntryId = $target.attr("data-control-entry-id");
    var dataDefinitionId = $target.attr("data-definition-id");
    if (typeof dataControlEntryId !== typeof undefined && dataControlEntryId !== false) {
      LITEPermissionsFinder.Triage._ajaxDisplayControlEntryModal(dataControlEntryId);
    } else if (typeof dataDefinitionId !== typeof undefined && dataDefinitionId !== false) {
      LITEPermissionsFinder.Triage._ajaxDisplayDefinitionModal(dataDefinitionId, $target.attr("data-definition-type"));
    } else {
      LITEPermissionsFinder.Triage._ajaxDisplayFailureModal();
    }
  },
  _ajaxDisplayControlEntryModal: function(controlEntryId) {
    $.ajax("/modal-content/control-entry/" + controlEntryId)
      .done(function(data) {
        LITECommon.Modal.displayModal($(data), "control entry");
      })
      .fail(LITEPermissionsFinder.Triage._ajaxDisplayFailureModal);
  },
  _ajaxDisplayDefinitionModal: function(definitionId, type) {
    $.ajax("/modal-content/" + type + "-definition/" + definitionId)
      .done(function(data) {
        var $data = $(data);
        LITEPermissionsFinder.Triage._bindModals($data);
        LITECommon.Modal.displayModal($data, "definition");
      })
      .fail(LITEPermissionsFinder.Triage._ajaxDisplayFailureModal);
  },
  _ajaxDisplayFailureModal: function() {
    LITECommon.Modal.displayModal("Sorry, cannot show this information", "error");
  }
};
