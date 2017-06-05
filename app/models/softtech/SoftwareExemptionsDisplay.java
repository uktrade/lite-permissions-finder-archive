package models.softtech;

import controllers.softtech.routes;

import java.util.Arrays;
import java.util.List;

public class SoftwareExemptionsDisplay {

  public final String formAction;
  public final String questionLabel;
  public final List<String> questionContent;

  public SoftwareExemptionsDisplay(SoftwareExemptionQuestion exemptionDisplayType) {
    if (exemptionDisplayType == SoftwareExemptionQuestion.Q1) {
      this.questionLabel = "Is your software in the public domain?";
      this.formAction = routes.SoftwareExemptionsController.handleSubmitQ1().url();
      this.questionContent = Arrays.asList("For example, available as a download that can be further shared without restriction.");
    }
    else if (exemptionDisplayType == SoftwareExemptionQuestion.Q2) {
      this.questionLabel = "Are you exporting information security software?";
      this.formAction = routes.SoftwareExemptionsController.handleSubmitQ2().url();
      this.questionContent = Arrays.asList("This means software that ensures the accessibility, confidentiality or integrity of information or communications. It includes software for cryptography, cryptographic activation, cryptanalysis, protection against compromising emanations and computer security.");
    }
    else {
      this.questionLabel = "Are you exporting any of the following?";
      this.formAction = routes.SoftwareExemptionsController.handleSubmitQ3().url();
      this.questionContent = Arrays.asList("Software that is available to buy from retail outlets, e.g. shops open to the public",
          "Software that the end user can install without expert help, e.g. a technician or engineer provided by the software's manufacturer",
          "The minimum necessary object code for the installation, operation, maintenance (checking) or repair of exported items");
    }

  }
}
