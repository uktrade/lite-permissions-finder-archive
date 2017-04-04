package utils.appcode;

import components.common.state.ContextParamProvider;

public class ApplicationCodeContextParamProvider extends ContextParamProvider {
  public static final String APPLICATION_CODE_CONTEXT_PARAM_NAME = "ctx_app_code";

  @Override
  public String getParamName() {
    return APPLICATION_CODE_CONTEXT_PARAM_NAME;
  }
}
