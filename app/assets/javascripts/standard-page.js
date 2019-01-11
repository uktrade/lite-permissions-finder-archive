$(document).ready(function() {
  $("#back-button").click(function() {
    window.history.back();
    return false;
  });
  $("#print").click(function() {
    window.print();
    return false;
  });
});
