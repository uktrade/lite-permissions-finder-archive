package models.softtech;

import java.util.Arrays;
import java.util.List;
import controllers.softtech.routes;

public class ExemptionsDisplay {

  public enum ExemptionDisplayType {
    Q1,
    Q2
  }

  public final String formAction;
  public final List<String> exemptions;

  public ExemptionsDisplay(ExemptionDisplayType exemptionDisplayType) {
    if (exemptionDisplayType == ExemptionDisplayType.Q1) {
      this.formAction = routes.ExemptionsController.handleSubmitQ1().url();
      this.exemptions = Arrays.asList("Available to buy from retail outlets (e.g. shops open to the public)",
          "In the public domain (free software that does not require permission or payment to use)");
    }
    else {
      this.formAction = routes.ExemptionsController.handleSubmitQ2().url();
      this.exemptions = Arrays.asList("Software that the user can install themselves, without your help",
          "Compiled source code that is the minimum needed to install, operate, maintain or repair exported items");
    }

  }
}
