var LITEOgelRegistration = {};


/**
 * Common utility functions
 */
LITEOgelRegistration.Utils = {

  /**
   * Checks if the given field is empty
   *
   * @param $field the field to check
   * @returns {boolean} true if the field is empty, otherwise false
   */
  isFieldEmpty: function isFieldEmpty($field) {
    return !$field.val() || $field.val().length <= 0;
  },

  /**
   * Sets up the Spinner provided by spin.js library.
   *
   * @param elementId the id of the DIV element which will be the container for the spinner.
   * @private
   */
  _setupSpinner: function setupSpinner(elementId) {
    var opts = {
      lines: 11, // The number of lines to draw
      length: 9, // The length of each line
      width: 6, // The line thickness
      radius: 12, // The radius of the inner circle
      scale: 0.75, // Scales overall size of the spinner
      corners: 1, // Corner roundness (0..1)
      color: '#00823b', // #rgb or #rrggbb or array of colors
      opacity: 0.25, // Opacity of the lines
      rotate: 0, // The rotation offset
      direction: 1, // 1: clockwise, -1: counterclockwise
      speed: 1, // Rounds per second
      trail: 60, // Afterglow percentage
      fps: 20, // Frames per second when using setTimeout() as a fallback for CSS
      zIndex: 2e9, // The z-index (defaults to 2000000000)
      className: 'spinner', // The CSS class to assign to the spinner
      top: '50%', // Top position relative to parent
      left: '50%', // Left position relative to parent
      shadow: false, // Whether to render a shadow
      hwaccel: false, // Whether to use hardware acceleration
      position: 'absolute' // Element positioning
    };
    var target = document.getElementById(elementId);
    var spinner = new Spinner(opts).spin(target);
  }

};


/**
 * Admin Approval Interval
 */
LITEOgelRegistration.AdminApprovalInterval = {

  setupPage: function () {
    var route = jsRoutes.controllers.AjaxAdminApprovalController.pollStatus($("input[name=ctx_transaction]").val());
    LITEOgelRegistration.AdminApprovalInterval._pollAdminApprovalStatus(route);
    setTimeout(function(){
      $('#pleaseWaitHeading').addClass('hidden');
      $('#longApprovalMessage').removeClass('hidden');
    }, 15000);
  },

  /**
   * Polls the current Admin Approval status via an AJAX call
   *
   * @param route the endpoint to call
   * @private
   */
  _pollAdminApprovalStatus: function pollAdminApprovalStatus(route) {
    setTimeout(function () {
      var approvalCompleted;
      $.ajax({
        url: route.url,
        type: route.type,
        success: function (data) {
          approvalCompleted = data.complete;
        },
        error: function (jqXHR, textStatus, errorThrown) {
          console.log("Poll admin approval status error - " + errorThrown);
        },
        complete: function () {
          if (!approvalCompleted) {
            pollAdminApprovalStatus(route);
          } else {
            $("#adminApprovalInterval").submit();
          }
        },
        timeout: 2000
      });
    }, 3000);
  }
};


/**
 * Address Lookup
 */
LITEOgelRegistration.AddressLookupContainer = {

  setupPage: function () {

    var $addressListField = $("#addressList");
    var $houseNumberField = $("#houseNumber");
    var $postcodeField = $("#postcode");

    var $addressSearchButton = $("#addressSearch");
    var $generalErrorDiv = $("#generalError");
    var $selectAddressButton = $("#selectAddress");
    var $addressDetailsContainer = $("#addressDetailsContainer");

    LITEOgelRegistration.AddressLookupContainer._initialiseAddressDetails($addressDetailsContainer);

    // Handles the address search button click
    $addressSearchButton.click(function (e) {
      e.preventDefault();

      var $errorContainer = $("#errorContainer");
      var $addressLookupSpinner = $("#addressLookupSpinner");

      LITEOgelRegistration.AddressLookupContainer._hideAddressList();
      LITEOgelRegistration.AddressLookupContainer._hideAddressDetails();

      // clear errors
      $errorContainer.empty();
      $generalErrorDiv.empty();
      LITEOgelRegistration.AddressLookupContainer._removeFieldError($postcodeField);


      var houseNumber = $houseNumberField.val();
      var postcode = $postcodeField.val();

      if (LITECommon.ClientSideValidation.validateForm($addressSearchButton.parents('form'), $addressSearchButton)) {
        // address lookup AJAX call
        var route = jsRoutes.controllers.AjaxAddressLookupController.addressLookup(houseNumber, postcode);
        $.ajax({
          url: route.url, type: route.type,

          beforeSend: function () {
            // disable search button and show spinner
            $addressSearchButton.prop("disabled", true);
            $addressLookupSpinner.show();
            LITEOgelRegistration.Utils._setupSpinner("addressLookupSpinner");
          },

          success: function (result) {
            if (result.status == "address-list") { // populate and show address list
              for (var i = 0; i < result.data.length; i++) {
                $addressListField.append("<option value=" + result.data[i].addressEncoded + ">" + result.data[i].addressSummary + "</option>");
              }
              $("#addressListContainer").show();
            }

            else if (result.status == "address-details") { // populate and show address details
              LITEOgelRegistration.AddressLookupContainer._populateAddressDetails(result.data);
              $addressDetailsContainer.show();

            } else { // display general error message
              var errorHTML = result.errorMessage;
              errorHTML += "<br>Alternatively, <button id=\"manualAddress\" name=\"submit\" type=\"submit\" value=\"manualAddress\" class=\"link underlined generalErrorMsg\">enter an address manually</button>.";
              $generalErrorDiv.html(errorHTML);
            }
          },

          complete: function () {
            // enable search button and hide spinner
            $addressSearchButton.prop("disabled", false);
            $addressLookupSpinner.empty();
            $addressLookupSpinner.hide();
          }
        });

      }

    });

    // Handles the general error manual address link click
    $generalErrorDiv.on("click", ".link", function (e) {
      var target = e.currentTarget.id;
      if (target == 'manualAddress') {
        e.preventDefault();

        LITEOgelRegistration.AddressLookupContainer._hideAddressList();

        // Copy address lookup houseNumber and postcode to address details
        $("#addressLine1").val($houseNumberField.val());
        $("#addressPostcode").val($postcodeField.val());

        // clear errors
        $generalErrorDiv.empty();

        $addressDetailsContainer.show();
      }
    });

    // Handles the select address button click
    $selectAddressButton.click(function (e) {

      e.preventDefault();

      if (LITECommon.ClientSideValidation.validateForm($selectAddressButton.parents('form'), $selectAddressButton)) {
        var selectedAddress = $addressListField.val();

        var r = jsRoutes.controllers.AjaxAddressLookupController.addressSelection(selectedAddress);

        $.ajax({
          url: r.url, type: r.type,
          beforeSend: function () {
            $selectAddressButton.prop("disabled", true);
          },
          success: function (data) {
            LITEOgelRegistration.AddressLookupContainer._populateAddressDetails(data);
            LITEOgelRegistration.AddressLookupContainer._hideAddressList();
            $addressDetailsContainer.show();
          },
          complete: function () {
            $selectAddressButton.prop("disabled", false);
          }
        });

      }

    });

    // Handles the none of the above link click
    $("#noneOfAboveAddresses").click(function (e) {

      e.preventDefault();

      $("#errorContainer").empty();

      LITEOgelRegistration.AddressLookupContainer._hideAddressList();

      $("#addressPostcode").val($postcodeField.val());
      $addressDetailsContainer.show();

    });
  },

  _initialiseAddressDetails: function initialiseAddressDetails($addressDetails) {
    LITECommon.countrySelectInitialise($addressDetails.find("#addressCountry > select"));
  },

  /**
   * Hides and clears the address list field
   *
   * @private
   */
  _hideAddressList: function hideAddressList() {
    $("#addressListContainer").hide();
    var $addressListField = $("#addressList");
    LITEOgelRegistration.AddressLookupContainer._removeFieldError($addressListField);
    $addressListField.html("");
    $("#errorContainer").empty();
  },

  /**
   * Hides and clears the address details fields
   *
   * @private
   */
  _hideAddressDetails: function hideAddressDetails() {
    $("#addressDetailsContainer").hide();
    var fields = [$("#addressLine1"), $("#addressLine2"), $("#addressTown"), $("#addressCounty"), $("#addressPostcode")];
    fields.forEach(function ($item) {
      $item.val('');
      LITEOgelRegistration.AddressLookupContainer._removeFieldError($item);
    });
  },

  /**
   * Populates the address details field with the given address
   *
   * @param address
   * @private
   */
  _populateAddressDetails: function populateAddressDetails(address) {
    $("#addressLine1").val(address.line1);
    $("#addressLine2").val(address.line2);
    $("#addressTown").val(address.town);
    $("#addressCounty").val(address.county);
    $("#addressPostcode").val(address.postcode);
  },

  /**
   * Removes the errors from the field element
   *
   * @param element
   * @private
   */
  _removeFieldError: function removeFieldError($element) {
    var $errorDiv = $element.closest(".form-group");
    $errorDiv.removeClass("error");
    $errorDiv.find("p.error-message").remove();
  }

};


/**
 * Companies House Search
 */
LITEOgelRegistration.CompaniesHouseSearch = {
  setupPage: function () {
    $("#companiesHouseSearch").submit(function () { // Validate Companies House Search form submission

      // Set custom function for Client side validation
      LITECommon.ClientSideValidation.setValidationFunction(function() {
        var validationFailures = [];

        if (LITEOgelRegistration.Utils.isFieldEmpty($("#companyName"))) {
          var $form = $(event.target);
          var $triggeringElement = $(document.activeElement);
          validationFailures = LITECommon.ClientSideValidation.standardValidation($form, $triggeringElement);
        }

        return validationFailures;
      });

    });
  }
};


/**
 * Companies House Search Results
 */
LITEOgelRegistration.CompaniesHouseSearchResults = {
  setupPage: function() {
    $("#showMoreCompanies").click(function (e) {

      e.preventDefault();

      var $companiesHouseOptionsContainer = $("#companiesHouseOptionsContainer");
      var $showMoreCompaniesContainer = $("#showMoreCompaniesContainer");
      var $showMoreCompaniesSpinner = $("#showMoreCompaniesSpinner");
      var $showMoreCompaniesButton = $("#showMoreCompanies");

      var searchParam = $("#searchParam").val();
      var currentPage = parseInt($("#currentPage").val());

      var route = jsRoutes.controllers.AjaxCompaniesHouseController.companiesHouseOptions(searchParam, ++currentPage);

      $.ajax({
        url: route.url, type: route.type,
        beforeSend: function () {
          $showMoreCompaniesContainer.hide();
          LITEOgelRegistration.Utils._setupSpinner("showMoreCompaniesSpinner");
          $showMoreCompaniesSpinner.show();
          LITEOgelRegistration.CompaniesHouseSearchResults._clearCompaniesHouseOptionsErrors();
        },
        success: function (data) {
          $companiesHouseOptionsContainer.append(data);
          LITECommon.initialiseSelectionButtons();
          $("#currentPage").val(currentPage);
        },
        error: function () {
          $showMoreCompaniesButton.remove();
          $("#companyNotFoundContainer").remove();
          $("#companyLookupFailureContainer").show();
        },
        complete: function () {
          $showMoreCompaniesSpinner.empty();
          $showMoreCompaniesSpinner.hide();
          $showMoreCompaniesContainer.show();

          if (currentPage >= 5) {
            $showMoreCompaniesContainer.html("<p class=\"generalErrorMsg\">Sorry, we are only able to show you the first 50 results.</p>");
            $showMoreCompaniesButton.remove();
          }
        }
      });
    });
  },

  _clearCompaniesHouseOptionsErrors: function clearCompaniesHouseOptionsErrors() {
    var $companiesHouseOptionsContainer = $("#companiesHouseOptionsContainer");
    LITEOgelRegistration.AddressLookupContainer._removeFieldError($companiesHouseOptionsContainer);
    $("div.error-summary").remove();
  }
};

/**
 * OGEL Registration Interval
 */
LITEOgelRegistration.RegistrationInterval = {

  setupPage: function () {
    var route = jsRoutes.controllers.AjaxRegisterOgelController.pollStatus($("input[name=ctx_transaction]").val());
    LITEOgelRegistration.RegistrationInterval._pollRegistrationStatus(route);
    setTimeout(function(){
      $('#pleaseWaitHeading').addClass('hidden');
      $('#longRegistrationMessage').removeClass('hidden');
    }, 15000);
  },

  /**
   * Polls the current OGEL Registration status via an AJAX call
   *
   * @param route the endpoint to call
   * @private
   */
  _pollRegistrationStatus: function pollRegistrationStatus(route) {
    setTimeout(function () {
      var registrationCompleted;
      $.ajax({
        url: route.url,
        type: route.type,
        success: function (data) {
          registrationCompleted = data.complete;
        },
        error: function (jqXHR, textStatus, errorThrown) {
          console.log("Poll registration submission status error - " + errorThrown);
        },
        complete: function () {
          if (registrationCompleted === true) {
            $("#registrationInterval").submit();
          } else {
            pollRegistrationStatus(route);
          }
        },
        timeout: 2000
      });
    }, 3000);
  }
};


/**
 * User Account Sites
 */
LITEOgelRegistration.UserAccountSites = {
  setupPage: function() {

    // Handles Select all sites link click
    $("#selectAllSites").click(function (e) {
      e.preventDefault();
      // set all unchecked checkboxes to checked
      $("input:checkbox:not(:checked)").click();
    });
  }
};






