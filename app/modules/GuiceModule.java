package modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import components.common.CommonGuiceModule;
import components.common.cache.CountryCacheScheduler;
import components.common.cache.CountryProvider;
import components.common.cache.UpdateCountryCacheActor;
import components.common.client.CountryServiceClient;
import components.common.journey.JourneyDefinitionBuilder;
import components.common.journey.JourneySerialiser;
import components.common.persistence.RedisKeyConfig;
import components.persistence.PermissionsFinderDao;
import importcontent.ImportJourneyDefinitionBuilder;
import journey.ExportJourneyDefinitionBuilder;
import play.Configuration;
import play.Environment;
import play.libs.akka.AkkaGuiceSupport;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Named;

public class GuiceModule extends AbstractModule implements AkkaGuiceSupport {

  private Environment environment;

  private Configuration configuration;

  public GuiceModule(Environment environment, Configuration configuration) {
    this.environment = environment;
    this.configuration = configuration;
  }

  @Override
  protected void configure() {

    install(new CommonGuiceModule(configuration));

    // searchService
    bindConstant().annotatedWith(Names.named("searchServiceAddress"))
        .to(configuration.getString("searchService.address"));
    bindConstant().annotatedWith(Names.named("searchServiceTimeout"))
        .to(configuration.getString("searchService.timeout"));

    // controlCodeService
    bindConstant().annotatedWith(Names.named("controlCodeServiceAddress"))
        .to(configuration.getString("controlCodeService.address"));
    bindConstant().annotatedWith(Names.named("controlCodeServiceTimeout"))
        .to(configuration.getString("controlCodeService.timeout"));

    // countryService
    bindConstant().annotatedWith(Names.named("countryServiceAddress"))
        .to(configuration.getString("countryService.address"));
    bindConstant().annotatedWith(Names.named("countryServiceTimeout"))
        .to(configuration.getString("countryService.timeout"));

    bindConstant().annotatedWith(Names.named("countryCacheExpiry"))
        .to(configuration.getString("countryCacheExpiry"));
    bindConstant().annotatedWith(Names.named("countryCacheKey"))
        .to(configuration.getString("countryCacheKey"));

    bind(CountryProvider.class).asEagerSingleton();
    bind(CountryCacheScheduler.class).asEagerSingleton();
    bindActor(UpdateCountryCacheActor.class, "updateCountryCacheActor");

    // ogelService
    bindConstant().annotatedWith(Names.named("ogelServiceAddress"))
        .to(configuration.getString("ogelService.address"));
    bindConstant().annotatedWith(Names.named("ogelServiceTimeout"))
        .to(configuration.getString("ogelService.timeout"));

    // notificationService
    bindConstant().annotatedWith(Names.named("notificationServiceAddress"))
        .to(configuration.getString("notificationService.address"));
    bindConstant().annotatedWith(Names.named("notificationServiceTimeout"))
        .to(configuration.getString("notificationService.timeout"));

    // ogelRegistration
    bindConstant().annotatedWith(Names.named("ogelRegistrationServiceAddress"))
        .to(configuration.getString("ogelRegistrationService.address"));
    bindConstant().annotatedWith(Names.named("ogelRegistrationServiceTimeout"))
        .to(configuration.getString("ogelRegistrationService.timeout"));
    bindConstant().annotatedWith(Names.named("ogelRegistrationServiceSharedSecret"))
        .to(configuration.getString("ogelRegistrationService.sharedSecret"));

    bind(RedisKeyConfig.class)
        .annotatedWith(Names.named("applicationCodeDaoHash"))
        .toInstance(createApplicationCodeKeyConfig());

    bind(JourneySerialiser.class).to(PermissionsFinderDao.class);
  }

  private RedisKeyConfig createApplicationCodeKeyConfig() {
    Configuration daoConfig = configuration.getConfig("redis.applicationCodeDaoHash");
    return new RedisKeyConfig(daoConfig.getString("keyPrefix"), daoConfig.getString("hashName"),
        daoConfig.getInt("ttlSeconds"));
  }

  @Provides
  public Collection<JourneyDefinitionBuilder> provideJourneyDefinitionBuilders() {
    ImportJourneyDefinitionBuilder importJourneyDefinitionBuilder = new ImportJourneyDefinitionBuilder();
    importJourneyDefinitionBuilder.initStages();

    return Arrays.asList(new ExportJourneyDefinitionBuilder(), importJourneyDefinitionBuilder);
  }

  @Provides
  CountryServiceClient provideCountryServiceClient(HttpExecutionContext httpContext, WSClient wsClient,
                                                   @Named("countryServiceAddress") String address,
                                                   @Named("countryServiceTimeout") int timeout,
                                                   ObjectMapper mapper) {
    return new CountryServiceClient(httpContext, wsClient, address + "/countries/set/export-control", timeout, mapper);
  }

  @Provides
  @Named("countryProviderExport")
  CountryProvider provideCountryServiceExportClient(HttpExecutionContext httpContext, WSClient wsClient,
                                                         @Named("countryServiceAddress") String address,
                                                         @Named("countryServiceTimeout") int timeout,
                                                         ObjectMapper mapper) {
    CountryProvider provider = new CountryProvider(new CountryServiceClient(httpContext, wsClient, address + "/countries/set/export-control", timeout, mapper));
    provider.loadCountries();
    return provider;
  }

  @Provides
  @Named("countryProviderEu")
  CountryProvider provideCountryServiceEuClient(HttpExecutionContext httpContext, WSClient wsClient,
                                                     @Named("countryServiceAddress") String address,
                                                     @Named("countryServiceTimeout") int timeout,
                                                     ObjectMapper mapper) {
    CountryProvider provider = new CountryProvider(new CountryServiceClient(httpContext, wsClient, address + "/countries/group/eu", timeout, mapper));
    provider.loadCountries();
    return provider;
  }
}