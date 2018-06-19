import com.google.inject.Inject;
import components.common.CommonContextActionSetup;
import components.common.state.ContextParamManager;
import play.i18n.MessagesApi;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

public class ContextAction extends Action<Void> {

  private final ContextParamManager contextParamManager;
  private final MessagesApi messagesApi;

  @Inject
  public ContextAction(ContextParamManager contextParamManager, MessagesApi messagesApi) {
    this.contextParamManager = contextParamManager;
    this.messagesApi = messagesApi;
  }

  @Override
  public CompletionStage<Result> call(Http.Context ctx) {

    // Add a reference to the ContextParamManager to the context so views can see it (DI workaround)
    ctx.args.put(ContextParamManager.CTX_PARAM_NAME, contextParamManager);

    ctx.args.put(CommonContextActionSetup.CTX_MESSAGE_API_NAME, messagesApi);

    return delegate.call(ctx);
  }
}



