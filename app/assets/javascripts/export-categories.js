var LITEPermissionsFinder = LITEPermissionsFinder || {};

LITEPermissionsFinder.ExportCategories = {
  setupPage: function() {
    LITEPermissionsFinder.ExportCategories._bindAnalyticsTriggers();
  },
  _bindAnalyticsTriggers: function() {
    $("button[name='category']").parents('details').children('summary').click(function () {
      if (!$(this).parent("details").is("[open]")) {
        var category = $(this).siblings().find("button[name='category']").first().val();
        _paq.push(['trackEvent', 'exportCategories', 'openedCategory', category]);
      }
    });
  }
};
