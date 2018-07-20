package modules;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import components.auth.SamlModule;
import components.client.ApplicableOgelServiceClient;
import components.client.ApplicableOgelServiceClientImpl;
import components.client.CustomerServiceClient;
import components.client.CustomerServiceClientImpl;
import components.client.OgelServiceClient;
import components.client.OgelServiceClientImpl;
import components.client.PermissionsServiceClient;
import components.client.PermissionsServiceClientImpl;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.GlobalDefinitionDao;
import components.cms.dao.JourneyDao;
import components.cms.dao.LocalDefinitionDao;
import components.cms.dao.NoteDao;
import components.cms.dao.RelatedControlEntryDao;
import components.cms.dao.SessionDao;
import components.cms.dao.SessionOutcomeDao;
import components.cms.dao.SessionStageDao;
import components.cms.dao.StageAnswerDao;
import components.cms.dao.StageDao;
import components.cms.dao.impl.ControlEntryDaoImpl;
import components.cms.dao.impl.GlobalDefinitionDaoImpl;
import components.cms.dao.impl.JourneyDaoImpl;
import components.cms.dao.impl.LocalDefinitionDaoImpl;
import components.cms.dao.impl.NoteDaoImpl;
import components.cms.dao.impl.RelatedControlEntryDaoImpl;
import components.cms.dao.impl.SessionDaoImpl;
import components.cms.dao.impl.SessionOutcomeDaoImpl;
import components.cms.dao.impl.SessionStageDaoImpl;
import components.cms.dao.impl.StageAnswerDaoImpl;
import components.cms.dao.impl.StageDaoImpl;
import components.common.auth.SpireAuthManager;
import components.common.cache.CountryProvider;
import components.common.cache.UpdateCountryCacheActor;
import components.common.client.CountryServiceClient;
import components.common.client.userservice.UserServiceClientBasicAuth;
import components.common.persistence.RedisKeyConfig;
import components.common.persistence.StatelessRedisDao;
import components.services.AccountService;
import components.services.AccountServiceImpl;
import components.services.AnswerConfigService;
import components.services.AnswerConfigServiceImpl;
import components.services.AnswerViewService;
import components.services.AnswerViewServiceImpl;
import components.services.BreadcrumbViewService;
import components.services.BreadcrumbViewServiceImpl;
import components.services.FlashService;
import components.services.FlashServiceImpl;
import components.services.ProgressViewService;
import components.services.ProgressViewServiceImpl;
import components.services.RenderService;
import components.services.RenderServiceImpl;
import components.services.SessionOutcomeService;
import components.services.SessionOutcomeServiceImpl;
import components.services.UserPrivilegeService;
import components.services.UserPrivilegeServiceImpl;
import filters.common.JwtRequestFilter;
import filters.common.JwtRequestFilterConfig;
import models.template.AnalyticsConfig;
import models.template.FeedbackConfig;
import models.template.DashboardConfig;
import modules.common.RedisSessionStoreModule;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.skife.jdbi.v2.DBI;
import play.Environment;
import play.db.Database;
import play.libs.akka.AkkaGuiceSupport;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import triage.cache.CachePopulationService;
import triage.cache.CachePopulationServiceImpl;
import triage.cache.CacheValidator;
import triage.cache.CacheValidatorImpl;
import triage.cache.JourneyConfigFactory;
import triage.cache.JourneyConfigFactoryImpl;
import triage.cache.StartupCachePopulationActor;
import triage.config.ControllerConfigService;
import triage.config.ControllerConfigServiceImpl;
import triage.config.DefinitionConfigService;
import triage.config.DefinitionConfigServiceImpl;
import triage.config.JourneyConfigService;
import triage.config.JourneyConfigServiceImpl;
import triage.session.SessionService;
import triage.session.SessionServiceImpl;
import triage.text.HtmlRenderService;
import triage.text.HtmlRenderServiceImpl;
import triage.text.ParserLookupService;
import triage.text.ParserLookupServiceDaoImpl;
import triage.text.RichTextParser;
import triage.text.RichTextParserImpl;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;

public class GuiceModule extends AbstractModule implements AkkaGuiceSupport {

  private final Environment environment;
  private final Config config;

  public GuiceModule(Environment environment, Config config) {
    this.environment = environment;
    this.config = config;
  }

  @Override
  protected void configure() {
    bind(AccountService.class).to(AccountServiceImpl.class);
    bind(DefinitionConfigService.class).to(DefinitionConfigServiceImpl.class).asEagerSingleton();
    bind(ProgressViewService.class).to(ProgressViewServiceImpl.class);
    bind(RenderService.class).to(RenderServiceImpl.class);
    bind(BreadcrumbViewService.class).to(BreadcrumbViewServiceImpl.class);
    bind(AnswerConfigService.class).to(AnswerConfigServiceImpl.class);
    bind(AnswerViewService.class).to(AnswerViewServiceImpl.class);
    bind(HtmlRenderService.class).to(HtmlRenderServiceImpl.class);
    bind(JourneyConfigService.class).to(JourneyConfigServiceImpl.class).asEagerSingleton();
    bind(ControllerConfigService.class).to(ControllerConfigServiceImpl.class);
    bind(RichTextParser.class).to(RichTextParserImpl.class);
    bind(ParserLookupService.class).to(ParserLookupServiceDaoImpl.class);
    bind(SessionService.class).to(SessionServiceImpl.class);
    bind(CacheValidator.class).to(CacheValidatorImpl.class).asEagerSingleton();
    bind(JourneyConfigFactory.class).to(JourneyConfigFactoryImpl.class);
    bind(CachePopulationService.class).to(CachePopulationServiceImpl.class);
    bind(StartupCachePopulationActor.class).asEagerSingleton();
    bind(PermissionsServiceClient.class).to(PermissionsServiceClientImpl.class);
    bind(ApplicableOgelServiceClient.class).to(ApplicableOgelServiceClientImpl.class);
    bind(CustomerServiceClient.class).to(CustomerServiceClientImpl.class);
    bind(OgelServiceClient.class).to(OgelServiceClientImpl.class);
    bind(SessionOutcomeService.class).to(SessionOutcomeServiceImpl.class);
    bind(UserPrivilegeService.class).to(UserPrivilegeServiceImpl.class);
    bind(FlashService.class).to(FlashServiceImpl.class);

    install(new SamlModule(config));
    install(new RedisSessionStoreModule(environment, config));

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

    bindConstant().annotatedWith(Names.named("basicAuthUser"))
        .to(config.getString("basicAuth.user"));

    bindConstant().annotatedWith(Names.named("basicAuthPassword"))
        .to(config.getString("basicAuth.password"));

    bindConstant().annotatedWith(Names.named("basicAuthRealm"))
        .to(config.getString("basicAuth.realm"));

    bindConstant().annotatedWith(Names.named("permissionsServiceAddress")).to(config.getString("permissionsService.address"));
    bindConstant().annotatedWith(Names.named("permissionsServiceTimeout")).to(config.getInt("permissionsService.timeout"));

    bindConstant().annotatedWith(Names.named("customerServiceAddress")).to(config.getString("customerService.address"));
    bindConstant().annotatedWith(Names.named("customerServiceTimeout")).to(config.getInt("customerService.timeout"));

    bindConstant().annotatedWith(Names.named("userServiceAddress")).to(config.getString("userService.address"));
    bindConstant().annotatedWith(Names.named("userServiceTimeout")).to(config.getString("userService.timeout"));
    bindConstant().annotatedWith(Names.named("userServiceCredentials")).to(config.getString("userService.credentials"));

    bindConstant().annotatedWith(Names.named("dashboardUrl")).to(config.getString("dashboard.url"));
    bindConstant().annotatedWith(Names.named("permissionsFinderUrl")).to(config.getString("permissionsFinderUrl"));

    bindConstant().annotatedWith(Names.named("jwtSharedSecret")).to(config.getString("jwtSharedSecret"));

    bindConstant().annotatedWith(Names.named("ecjuEmailAddress")).to(config.getString("ecjuEmailAddress"));

    // CMS dao's
    bind(ControlEntryDao.class).to(ControlEntryDaoImpl.class);
    bind(GlobalDefinitionDao.class).to(GlobalDefinitionDaoImpl.class);
    bind(JourneyDao.class).to(JourneyDaoImpl.class);
    bind(LocalDefinitionDao.class).to(LocalDefinitionDaoImpl.class);
    bind(NoteDao.class).to(NoteDaoImpl.class);
    bind(StageAnswerDao.class).to(StageAnswerDaoImpl.class);
    bind(StageDao.class).to(StageDaoImpl.class);
    bind(RelatedControlEntryDao.class).to(RelatedControlEntryDaoImpl.class);

    bind(SessionDao.class).to(SessionDaoImpl.class);
    bind(SessionStageDao.class).to(SessionStageDaoImpl.class);
    bind(SessionOutcomeDao.class).to(SessionOutcomeDaoImpl.class);

    requestInjection(this);
  }

  @Provides
  @Named("notificationServiceAwsSqsQueueUrl")
  public String provideNotificationServiceAwsSqsQueueUrl() {
    return config.getString("notificationService.aws.sqsQueueUrl");
  }

  @Provides
  AmazonSQS provideAmazonSqs() {
    String region = config.getString("aws.region");
    AWSCredentialsProvider awsCredentialsProvider = getAwsCredentials();
    return AmazonSQSClientBuilder.standard()
        .withRegion(region)
        .withCredentials(awsCredentialsProvider)
        .build();
  }

  private AWSCredentialsProvider getAwsCredentials() {
    String profileName = config.getString("aws.credentials.profileName");
    String accessKey = config.getString("aws.credentials.accessKey");
    String secretKey = config.getString("aws.credentials.secretKey");
    if (StringUtils.isNoneBlank(profileName)) {
      return new ProfileCredentialsProvider(profileName);
    } else if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(secretKey)) {
      throw new RuntimeException("accessKey and secretKey must both be specified if no profile name is specified");
    } else {
      return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
    }
  }

  @Provides
  public StatelessRedisDao provideStatelessRedisDao(RedissonClient redissonClient) {
    RedisKeyConfig redisKeyConfig = new RedisKeyConfig(config.getString("redis.keyPrefix"),
        "licenceFinderDao", config.getInt("redis.hashTtlSeconds"));
    return new StatelessRedisDao(redisKeyConfig, redissonClient);
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
  @Named("scheduledJobExecutionContext")
  ExecutionContext provideScheduledJobExecutionContext(final ActorSystem system) {
    return system.dispatchers().lookup("scheduled-job-context");
  }

  @Provides
  @Singleton
  @Named("countryCacheActorRefEu")
  ActorRef provideCountryCacheActorRefEu(final ActorSystem system,
                                         @Named("countryProviderEu") CountryProvider countryProvider,
                                         @Named("scheduledJobExecutionContext") ExecutionContext executionContext) {
    return system.actorOf(Props.create(UpdateCountryCacheActor.class, () -> new UpdateCountryCacheActor(countryProvider, 30, executionContext)));
  }

  @Provides
  @Singleton
  @Named("countryCacheActorRefExport")
  ActorRef provideCountryCacheActorRefExport(final ActorSystem system,
                                             @Named("countryProviderExport") CountryProvider countryProvider,
                                             @Named("scheduledJobExecutionContext") ExecutionContext executionContext) {
    return system.actorOf(Props.create(UpdateCountryCacheActor.class, () -> new UpdateCountryCacheActor(countryProvider, 30, executionContext)));
  }

  @Inject
  public void initActorScheduler(final ActorSystem system,
                                 @Named("countryCacheActorRefEu") ActorRef countryCacheActorRefEu,
                                 @Named("countryCacheActorRefExport") ActorRef countryCacheActorRefExport,
                                 @Named("scheduledJobExecutionContext") ExecutionContext executionContext) {
    FiniteDuration delay = Duration.create(0, TimeUnit.MILLISECONDS);
    FiniteDuration frequency = Duration.create(1, TimeUnit.DAYS);
    system.scheduler().schedule(delay, frequency, countryCacheActorRefEu, "load", executionContext, ActorRef.noSender());
    system.scheduler().schedule(delay, frequency, countryCacheActorRefExport, "load", executionContext, ActorRef.noSender());
  }

  @Provides
  @Singleton
  public JwtRequestFilter provideJwtRequestFilterConfig(UserServiceClientBasicAuth basicAuthClient,
                                                        SpireAuthManager spireAuthManager,
                                                        @Named("jwtSharedSecret") String jwtSharedSecret) {
    JwtRequestFilterConfig filterConfig = new JwtRequestFilterConfig(jwtSharedSecret, "lite-permissions-finder");
    return new JwtRequestFilter(spireAuthManager, filterConfig, basicAuthClient);
  }

  @Provides
  @Singleton
  public DBI provideDataSourceDbi(Config config, Database database) {
    return new DBI(database.getUrl());
  }

  @Provides
  public AnalyticsConfig provideAnalyticsConfig(Config config) {
    if (!config.getIsNull("analytics.googleAnalyticsId") && StringUtils.isNotEmpty(config.getString("analytics.googleAnalyticsId"))) {
      return new AnalyticsConfig(config.getString("analytics.googleAnalyticsId"));
    } else {
      return new AnalyticsConfig(null);
    }
  }

  @Provides
  public FeedbackConfig provideFeedbackConfig(Config config) {
    if (!config.getIsNull("feedbackUrl") && StringUtils.isNotEmpty(config.getString("feedbackUrl"))) {
      return new FeedbackConfig(config.getString("feedbackUrl"));
    } else {
      return new FeedbackConfig(null);
    }
  }

  @Provides
  public DashboardConfig provideDashboardConfig(Config config) {
    if (!config.getIsNull("dashboard.url") && StringUtils.isNotEmpty(config.getString("dashboard.url"))) {
      return new DashboardConfig(config.getString("dashboard.url"));
    } else {
      return new DashboardConfig(null);
    }
  }
}