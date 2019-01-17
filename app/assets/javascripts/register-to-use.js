// Control entries
$(document).on("click", "a[href^='/view-past-selections']", function(e) {
	$.getJSON("/modal-content/past-selections", {
			sessionId: document.getElementById('sessionId').value
		})
		.done(function(data) {
			var text = "<table class='govuk-table'><tbody class='govuk-table__body'>";

			data.forEach(function(obj) {
				text = text + "<tr class='govuk-table__row'>";
				text = text + "<th class='govuk-table__header' scope='row'>" + obj.question + "</th>";
				text = text + "<td class='govuk-table__cell'>" + obj.answer + "</tr>";
				text = text + "</tr>";
			});

			text = text + "</tbody></table>";
			LITECommon.Modal.showModal("Selections", text);
		})
		.fail();

	return false;
});

// Hide content below it
$(".step-by-step-title").on("click", function() {
	$(this).next().toggle();
	$(this).parent().toggleClass("hide-line");
	if($(this).find("a").text() == 'Hide'){
		$(this).find("a").text('Show');
	} else {
		$(this).find("a").text('Hide');
	}
});
