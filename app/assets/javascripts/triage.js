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

	$.getJSON("/modal-content/control-entry/" + controlEntryId, {
			sessionId: "dbdead36-4b46-4f6b-a0bf-d47a228ac740"
		})
		.done(function(data) {
			var controlEntryUrl = "<a class='govuk-label govuk-link govuk-link--arrow' href='" + data.controlEntryUrl + "'>Go to this control entry</a>";

			var breadcrumbs = "<ol class='control-code-breadcrumbs'>";

			data.items.forEach(function(obj) {
				var text = (obj.text || "");
				if (text.length) {
					text = "<span class='govuk-!-font-weight-bold'>" + text + " </span>";
				}
				breadcrumbs = breadcrumbs + "<li>" + text + obj.description + "</li>";
			});

			if (data.description) {
				breadcrumbs = breadcrumbs + "<div class='control-code-breadcrumbs-notes'>" + data.description + "</div>";
			}

			breadcrumbs = breadcrumbs + "</ol>";

			LITECommon.Modal.showModal(data.items[data.items.length - 1].text, breadcrumbs + controlEntryUrl);
		})
		.fail();

	return false;
});
