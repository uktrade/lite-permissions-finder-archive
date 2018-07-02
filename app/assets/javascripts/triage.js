var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.Triage = {
  setupPage: function(sessionId) {
    LITEPermissionsFinder.Triage._bindModals($(document.body));
    this._sessionId = sessionId;
  },
  _sessionId: '',
  _bindModals: function($target) {
    $target.on("click", "a[data-control-entry-id], a[data-definition-id], a[data-modal-content-id]", LITEPermissionsFinder.Triage._modalLinkOnClick);
  },
  _modalLinkOnClick: function(event) {
    event.preventDefault();
    var $target = $(event.target);
    var dataControlEntryId = $target.attr("data-control-entry-id");
    var dataDefinitionId = $target.attr("data-definition-id");
    var dataModalContentId = $target.attr("data-modal-content-id");
    if (typeof dataControlEntryId !== typeof undefined && dataControlEntryId !== false) {
      LITEPermissionsFinder.Triage._ajaxDisplayControlEntryModal(dataControlEntryId);
    } else if (typeof dataDefinitionId !== typeof undefined && dataDefinitionId !== false) {
      LITEPermissionsFinder.Triage._ajaxDisplayDefinitionModal(dataDefinitionId, $target.attr("data-definition-type"));
    } else if (typeof dataModalContentId !== typeof undefined && dataModalContentId !== false) {
      LITEPermissionsFinder.Triage._ajaxDisplayModalContentModal(dataModalContentId);
    } else {
      LITEPermissionsFinder.Triage._ajaxDisplayFailureModal();
    }
  },
  _ajaxDisplayControlEntryModal: function(controlEntryId) {
    $.ajax("/modal-content/control-entry/" + controlEntryId, {data: {sessionId: this._sessionId}})
      .done(function(data) {
        var $data = $(data);
        LITEPermissionsFinder.Triage._bindModals($data);
        LITECommon.Modal.displayModal($data, "control entry");
      })
      .fail(LITEPermissionsFinder.Triage._ajaxDisplayFailureModal);
  },
  _ajaxDisplayDefinitionModal: function(definitionId, type) {
    $.ajax("/modal-content/definition/" + type + "/" + definitionId)
      .done(function(data) {
        var $data = $(data);
        LITEPermissionsFinder.Triage._bindModals($data);
        LITECommon.Modal.displayModal($data, "definition");
      })
      .fail(LITEPermissionsFinder.Triage._ajaxDisplayFailureModal);
  },
  _ajaxDisplayModalContentModal: function(modalContentId) {
    $.ajax("/modal-content/content/" + modalContentId)
      .done(function(data) {
        var $data = $(data);
        LITEPermissionsFinder.Triage._bindModals($data);
        LITECommon.Modal.displayModal($data, "content");
      })
      .fail(LITEPermissionsFinder.Triage._ajaxDisplayFailureModal);
  },
  _ajaxDisplayFailureModal: function() {
    LITECommon.Modal.displayModal('Sorry, this information can\'t be shown at the moment. Please <a href="#" class="close-modal">close</a> and try again.', "error");
  }
};

$(document).ready( function() {
  var sessionId = document.getElementById('sessionId').value;
  LITEPermissionsFinder.Triage.setupPage(sessionId);
});
