// Definitions
$(document).on("click", "a[href^='/view-definition']", function(e) {
	var link = $(this).attr("href");
	var type = link.substring(link.indexOf("/", 1) + 1, link.lastIndexOf("/"));
	var definitionId = link.substring(link.lastIndexOf("/") + 1);

	$.getJSON("/modal-content/definition/" + type + "/" + definitionId)
		.done(function(data) {
			LITECommon.Modal.showModal(data.term, data.definition);
		})
		.fail();

	return false;
});

// Control entries
$(document).on("click", "a[href^='/view-control-entry']", function(e) {
	var link = $(this).attr("href");
	var controlEntryId = link.substring(link.lastIndexOf("/") + 1);

	LITECommon.Modal.showModal("This needs to be built", "The control entry id is: " + controlEntryId);

	/*$.getJSON("/modal-content/control-entry/" + controlEntryId, { data: "banana" })
		.done(function(data) {
		})
		.fail();*/

	return false;
});
