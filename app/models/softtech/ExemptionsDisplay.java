package models.softtech;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import controllers.softtech.routes;

public class ExemptionsDisplay {

  public final String formAction;
  public final String pageTitle;
  public final String questionLabel;
  public final List<String> questionContent;

  public ExemptionsDisplay(ExemptionQuestion exemptionDisplayType) {
    if (exemptionDisplayType == ExemptionQuestion.Q1) {
      this.pageTitle = "Public domain software";
      this.questionLabel = "Is your software in the public domain, for example available as a free download that does not require permission or payment to use?";
      this.formAction = routes.ExemptionsController.handleSubmitQ1().url();
      this.questionContent = Collections.emptyList();
    }
    else if (exemptionDisplayType == ExemptionQuestion.Q2) {
      this.pageTitle = "Information security software";
      this.questionLabel = "Are you exporting information security software?";
      this.formAction = routes.ExemptionsController.handleSubmitQ2().url();
      this.questionContent = Arrays.asList("This means software that ensures the accessibility, confidentiality or integrity of information or communications. It includes software for cryptography, cryptographic activation, cryptanalysis, protection against compromising emanations and computer security.");
    }
    else {
      this.pageTitle = "Other software exemptions";
      this.questionLabel = "Are you exporting any of the following?";
      this.formAction = routes.ExemptionsController.handleSubmitQ3().url();
      this.questionContent = Arrays.asList("Software that is available to buy from retail outlets, e.g. shops open to the public",
          "Software that the end user can install without expert help, e.g. a technician or engineer provided by the software's manufacturer",
          "Source code that is needed to install, operate, maintain or repair exported items, but that cannot be used for anything else");
    }

  }
}
