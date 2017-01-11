package models.softtech;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import controllers.softtech.routes;

public class ExemptionsDisplay {

  public enum ExemptionDisplayType {
    Q1,
    Q2,
    Q3
  }

  public final String formAction;
  public final String pageTitle;
  public final String questionLabel;
  public final List<String> questionContent;

  public ExemptionsDisplay(ExemptionDisplayType exemptionDisplayType) {
    if (exemptionDisplayType == ExemptionDisplayType.Q1) {
      this.pageTitle = "Public domain software";
      this.questionLabel = "Is your software in the public domain, for example available as a free download that does not require permission or payment to use?";
      this.formAction = routes.ExemptionsController.handleSubmitQ1().url();
      this.questionContent = Collections.emptyList();
    }
    else if (exemptionDisplayType == ExemptionDisplayType.Q2) {
      this.pageTitle = "Information security software";
      this.questionLabel = "Are you exporting information security software?";
      this.formAction = routes.ExemptionsController.handleSubmitQ2().url();
      this.questionContent = Arrays.asList("This means software that ensures the accessibility, confidentiality or integrity of information or communications. It includes software for cryptography, cryptographic activation, cryptanalysis, protection against compromising emanations and computer security.");
    }
    else {
      this.pageTitle = "Dummy";
      this.questionLabel = "Do software exemptions apply?";
      this.formAction = routes.ExemptionsController.handleSubmitQ3().url();
      this.questionContent = Arrays.asList("Software that the user can install themselves, without your help",
          "Compiled source code that is the minimum needed to install, operate, maintain or repair exported items");
    }

  }
}
