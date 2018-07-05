$(document).ready(function() {
  $("#back-button").click(function() {
    window.history.back();
  });
  $("#print").click(function() {
    window.print();
    return false;
  });
});
