package components.cms.parser.model.navigation.column;

public class Redirect {
  private final boolean tooComplexForCodeFinder;

  public Redirect(boolean tooComplexForCodeFinder) {
    this.tooComplexForCodeFinder = tooComplexForCodeFinder;
  }

  public boolean isTooComplexForCodeFinder() {
    return tooComplexForCodeFinder;
  }
}
