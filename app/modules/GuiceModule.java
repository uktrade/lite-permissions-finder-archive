package modules;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import components.common.CommonGuiceModule;
import components.common.RedisGuiceModule;
import components.common.cache.CountryProvider;
import components.common.cache.UpdateCountryCacheActor;
import components.common.client.CountryServiceClient;
import components.common.journey.JourneyContextParamProvider;
import components.common.journey.JourneyDefinitionBuilder;
import components.common.journey.JourneySerialiser;
import components.common.persistence.RedisKeyConfig;
import components.common.state.ContextParamManager;
import components.common.transaction.TransactionContextParamProvider;
import importcontent.ImportJourneyDefinitionBuilder;
import journey.ExportJourneyDefinitionBuilder;
import journey.PermissionsFinderJourneySerialiser;
import journey.SubJourneyContextParamProvider;
import models.summary.SummaryService;
import models.summary.SummaryServiceImpl;
import org.pac4j.play.store.PlayCacheSessionStore;
import org.pac4j.play.store.PlaySessionStore;
import play.Environment;
import play.cache.SyncCacheApi;
import play.libs.akka.AkkaGuiceSupport;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import utils.appcode.ApplicationCodeContextParamProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;

public class GuiceModule extends AbstractModule implements AkkaGuiceSupport {

  private Environment environment;

  private Config config;

  public GuiceModule(Environment environment, Config config) {
    this.environment = environment;
    this.config = config;
  }

  @Override
  protected void configure() {

    install(new CommonGuiceModule(config));
    install(new RedisGuiceModule(config));

    // searchService
    bindConstant().annotatedWith(Names.named("searchServiceAddress"))
        .to(config.getString("searchService.address"));
    bindConstant().annotatedWith(Names.named("searchServiceTimeout"))
        .to(config.getString("searchService.timeout"));
    bindConstant().annotatedWith(Names.named("searchServiceCredentials"))
        .to(config.getString("searchService.credentials"));

    // controlCodeService
    bindConstant().annotatedWith(Names.named("controlCodeServiceAddress"))
        .to(config.getString("controlCodeService.address"));
    bindConstant().annotatedWith(Names.named("controlCodeServiceTimeout"))
        .to(config.getString("controlCodeService.timeout"));
    bindConstant().annotatedWith(Names.named("controlCodeServiceCredentials"))
        .to(config.getString("controlCodeService.credentials"));

    // countryService
    bindConstant().annotatedWith(Names.named("countryServiceAddress"))
        .to(config.getString("countryService.address"));
    bindConstant().annotatedWith(Names.named("countryServiceTimeout"))
        .to(config.getString("countryService.timeout"));
    bindConstant().annotatedWith(Names.named("countryServiceCredentials"))
        .to(config.getString("countryService.credentials"));

    // ogelService
    bindConstant().annotatedWith(Names.named("ogelServiceAddress"))
        .to(config.getString("ogelService.address"));
    bindConstant().annotatedWith(Names.named("ogelServiceTimeout"))
        .to(config.getString("ogelService.timeout"));
    bindConstant().annotatedWith(Names.named("ogelServiceCredentials"))
        .to(config.getString("ogelService.credentials"));

    // notificationService
    bindConstant().annotatedWith(Names.named("notificationServiceAddress"))
        .to(config.getString("notificationService.address"));
    bindConstant().annotatedWith(Names.named("notificationServiceTimeout"))
        .to(config.getString("notificationService.timeout"));
    bindConstant().annotatedWith(Names.named("notificationServiceCredentials"))
        .to(config.getString("notificationService.credentials"));

    // ogelRegistration
    bindConstant().annotatedWith(Names.named("ogelRegistrationServiceAddress"))
        .to(config.getString("ogelRegistrationService.address"));
    bindConstant().annotatedWith(Names.named("ogelRegistrationServiceTimeout"))
        .to(config.getString("ogelRegistrationService.timeout"));
    bindConstant().annotatedWith(Names.named("ogelRegistrationServiceSharedSecret"))
        .to(config.getString("ogelRegistrationService.sharedSecret"));

    bind(RedisKeyConfig.class)
        .annotatedWith(Names.named("applicationCodeDaoHash"))
        .toInstance(createApplicationCodeKeyConfig());

    bind(JourneySerialiser.class).to(PermissionsFinderJourneySerialiser.class);

    bindConstant().annotatedWith(Names.named("basicAuthUser"))
        .to(config.getString("basicAuth.user"));

    bindConstant().annotatedWith(Names.named("basicAuthPassword"))
        .to(config.getString("basicAuth.password"));

    bindConstant().annotatedWith(Names.named("basicAuthRealm"))
        .to(config.getString("basicAuth.realm"));

    bind(SummaryService.class).to(SummaryServiceImpl.class);

    requestInjection(this);
  }

  private RedisKeyConfig createApplicationCodeKeyConfig() {
    Config daoConfig = config.getConfig("redis.applicationCodeDaoHash");
    return new RedisKeyConfig(daoConfig.getString("keyPrefix"), daoConfig.getString("hashName"),
        daoConfig.getInt("ttlSeconds"));
  }

  @Provides
  public Collection<JourneyDefinitionBuilder> provideJourneyDefinitionBuilders(ExportJourneyDefinitionBuilder exportBuilder,
                                                                               ImportJourneyDefinitionBuilder importBuilder) {
    return Arrays.asList(exportBuilder, importBuilder);
  }

  @Provides
  @Singleton
  @Named("countryProviderExport")
  CountryProvider provideCountryServiceExportClient(HttpExecutionContext httpContext, WSClient wsClient,
                                                    @Named("countryServiceAddress") String address,
                                                    @Named("countryServiceTimeout") int timeout,
                                                    @Named("countryServiceCredentials") String credentials,
                                                    ObjectMapper mapper) {
    CountryServiceClient client = CountryServiceClient.buildCountryServiceSetClient(httpContext, wsClient, timeout, address, credentials, "export-control", mapper);
    return new CountryProvider(client);
  }

  @Provides
  @Singleton
  @Named("countryProviderEu")
  CountryProvider provideCountryServiceEuClient(HttpExecutionContext httpContext, WSClient wsClient,
                                                @Named("countryServiceAddress") String address,
                                                @Named("countryServiceTimeout") int timeout,
                                                @Named("countryServiceCredentials") String credentials,
                                                ObjectMapper mapper) {
    CountryServiceClient client = CountryServiceClient.buildCountryServiceGroupClient(httpContext, wsClient, timeout, address, credentials, "eu", mapper);
    return new CountryProvider(client);
  }

  @Provides
  @Singleton
  @Named("countryCacheActorRefEu")
  ActorRef provideCountryCacheActorRefEu(final ActorSystem system, @Named("countryProviderEu") CountryProvider countryProvider) {
    return system.actorOf(Props.create(UpdateCountryCacheActor.class, () -> new UpdateCountryCacheActor(countryProvider)));
  }

  @Provides
  @Singleton
  @Named("countryCacheActorRefExport")
  ActorRef provideCountryCacheActorRefExport(final ActorSystem system, @Named("countryProviderExport") CountryProvider countryProvider) {
    return system.actorOf(Props.create(UpdateCountryCacheActor.class, () -> new UpdateCountryCacheActor(countryProvider)));
  }

  @Inject
  public void initActorScheduler(final ActorSystem actorSystem, @Named("countryCacheActorRefEu") ActorRef countryCacheActorRefEu,
                                 @Named("countryCacheActorRefExport") ActorRef countryCacheActorRefExport) {
    FiniteDuration delay = Duration.create(0, TimeUnit.MILLISECONDS);
    FiniteDuration frequency = Duration.create(1, TimeUnit.DAYS);
    actorSystem.scheduler().schedule(delay, frequency, countryCacheActorRefEu, "EU country cache", actorSystem.dispatcher(), null);
    actorSystem.scheduler().schedule(delay, frequency, countryCacheActorRefExport, "EXPORT country cache", actorSystem.dispatcher(), null);
  }

  @Provides
  public ContextParamManager provideContextParamManager() {
    return new ContextParamManager(new JourneyContextParamProvider(), new TransactionContextParamProvider(), new SubJourneyContextParamProvider(), new ApplicationCodeContextParamProvider());
  }

  @Singleton
  @Provides
  public PlaySessionStore providePlaySessionStore(SyncCacheApi syncCacheApi) {
    PlayCacheSessionStore playCacheSessionStore = new PlayCacheSessionStore(syncCacheApi);
    playCacheSessionStore.setTimeout((int) TimeUnit.MINUTES.toSeconds(15));
    return playCacheSessionStore;
  }

}