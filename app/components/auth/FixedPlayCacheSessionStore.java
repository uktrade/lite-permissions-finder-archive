package components.auth;

import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.store.PlayCacheSessionStore;
import play.cache.SyncCacheApi;
import play.mvc.Http;

public class FixedPlayCacheSessionStore extends PlayCacheSessionStore {

  FixedPlayCacheSessionStore(SyncCacheApi cache) {
    super(cache);
  }

  @Override
  public boolean destroySession(final PlayWebContext context) {
    final Http.Session session = context.getJavaSession();
    final String sessionId = session.get(Pac4jConstants.SESSION_ID);
    if (sessionId != null) {
      //The built-in implementation clears the whole session but we only want remove the Pac4j session ID .
      //Otherwise the CSRF token is also cleared which breaks any other tabs the user has open.
      session.remove(Pac4jConstants.SESSION_ID);
      return true;
    }
    return false;
  }

}
