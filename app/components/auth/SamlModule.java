package components.auth;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import components.common.auth.SamlUtil;
import org.pac4j.play.LogoutController;
import org.pac4j.play.store.PlayCacheSessionStore;
import org.pac4j.play.store.PlaySessionStore;
import play.cache.NamedCache;
import play.cache.SyncCacheApi;

import java.util.concurrent.TimeUnit;

public class SamlModule extends AbstractModule {

  private final Config config;

  public SamlModule(Config config) {
    this.config = config;
  }

  @Override
  protected void configure() {
    String defaultUrl = controllers.routes.AuthorisationController.loggedOut().url();
    bind(LogoutController.class).toInstance(SamlUtil.createLogoutController(config, defaultUrl));
  }

  @Singleton
  @Provides
  public PlaySessionStore providePlaySessionStore(@NamedCache("pac4j-session-store") SyncCacheApi syncCacheApi) {
    PlayCacheSessionStore playCacheSessionStore = new FixedPlayCacheSessionStore(syncCacheApi);
    playCacheSessionStore.setTimeout((int) TimeUnit.MINUTES.toSeconds(config.getInt("pac4j.sessionTimeoutMinutes")));
    return playCacheSessionStore;
  }

  @Singleton
  @Provides
  public org.pac4j.core.config.Config provideConfig(PlaySessionStore playSessionStore, SamlAuthorizer samlAuthorizer) {
    return SamlUtil.buildConfig(config,
        playSessionStore,
        new SamlHttpActionAdaptor(),
        ImmutableMap.of(SamlAuthorizer.AUTHORIZER_NAME, samlAuthorizer));
  }

}
