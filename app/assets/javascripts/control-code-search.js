$("#search-box").on('input', function() {
	var value = $(this).val();
	if (!isEmptyOrSpaces(value)) {
		$.getJSON("/jump-to/search", {"query" : value}).done(function(data) {
			$("#results").empty();
			if (data.length) {
				data.forEach(function(obj) {
					$("#results").append("<div class='search-result'>" +
											"<a href='/jump-to/jump/" + obj.id + "'>" + obj.controlCode + "</a>" +
											"<p>" + obj.description + "</p>" +
										 "</div>");
				});
			} else {
				$("#results").append("<p class='no-results'>No results for '" + value + "'</p>");
			}
		});
	} else {
		$("#results").empty();
	}
});

function isEmptyOrSpaces(str){
    return str === null || str.match(/^ *$/) !== null;
}
