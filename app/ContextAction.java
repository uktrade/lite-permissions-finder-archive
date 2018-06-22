import com.google.inject.Inject;
import play.i18n.MessagesApi;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

public class ContextAction extends Action<Void> {

  public static final String CTX_MESSAGE_API_NAME = "message_api";

  private final MessagesApi messagesApi;

  @Inject
  public ContextAction(MessagesApi messagesApi) {
    this.messagesApi = messagesApi;
  }

  @Override
  public CompletionStage<Result> call(Http.Context ctx) {

    ctx.args.put(CTX_MESSAGE_API_NAME, messagesApi);

    return delegate.call(ctx);
  }
}



