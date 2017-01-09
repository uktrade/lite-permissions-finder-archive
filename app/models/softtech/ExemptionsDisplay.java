package models.softtech;

import java.util.Arrays;
import java.util.List;
import controllers.softtech.routes;

public class ExemptionsDisplay {

  public enum ExemptionDisplayType {
    Q1,
    Q2,
    Q3
  }

  public final String formAction;
  public final String questionLabel;
  public final List<String> exemptions;

  public ExemptionsDisplay(ExemptionDisplayType exemptionDisplayType) {
    if (exemptionDisplayType == ExemptionDisplayType.Q1) {
      this.questionLabel = "Do public domain software exemptions apply?";
      this.formAction = routes.ExemptionsController.handleSubmitQ1().url();
      this.exemptions = Arrays.asList("Available to buy from retail outlets (e.g. shops open to the public)",
          "In the public domain (free software that does not require permission or payment to use)");
    }
    else if (exemptionDisplayType == ExemptionDisplayType.Q2) {
      this.questionLabel = "Is software for information security?";
      this.formAction = routes.ExemptionsController.handleSubmitQ2().url();
      this.exemptions = Arrays.asList("Is your software for information security?");
    }
    else {
      this.questionLabel = "Do software exemptions apply?";
      this.formAction = routes.ExemptionsController.handleSubmitQ3().url();
      this.exemptions = Arrays.asList("Software that the user can install themselves, without your help",
          "Compiled source code that is the minimum needed to install, operate, maintain or repair exported items");
    }

  }
}
