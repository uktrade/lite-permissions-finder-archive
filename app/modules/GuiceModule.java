package modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import components.common.CommonGuiceModule;
import components.common.journey.JourneyDefinitionBuilder;
import components.common.journey.JourneyManager;
import play.Configuration;
import play.Environment;

public class GuiceModule extends AbstractModule{

  private Environment environment;

  private Configuration configuration;

  public GuiceModule(Environment environment, Configuration configuration) {
    this.environment = environment;
    this.configuration = configuration;
  }

  @Override
  protected void configure() {

    install(new CommonGuiceModule(environment, configuration));

    // controlCodeSearchService
    bindConstant().annotatedWith(Names.named("controlCodeSearchServiceHost"))
        .to(configuration.getString("controlCodeSearchService.hostname"));
    bindConstant().annotatedWith(Names.named("controlCodeSearchServicePort"))
        .to(configuration.getString("controlCodeSearchService.port"));
    bindConstant().annotatedWith(Names.named("controlCodeSearchServiceTimeout"))
        .to(configuration.getString("controlCodeSearchService.timeout"));

    // controlCodeFrontendService
    bindConstant().annotatedWith(Names.named("controlCodeFrontendServiceHost"))
        .to(configuration.getString("controlCodeFrontendService.hostname"));
    bindConstant().annotatedWith(Names.named("controlCodeFrontendServicePort"))
        .to(configuration.getString("controlCodeFrontendService.port"));
    bindConstant().annotatedWith(Names.named("controlCodeFrontendServiceTimeout"))
        .to(configuration.getString("controlCodeFrontendService.timeout"));

    // countryService
    bindConstant().annotatedWith(Names.named("countryServiceHost"))
        .to(configuration.getString("countryService.hostname"));
    bindConstant().annotatedWith(Names.named("countryServicePort"))
        .to(configuration.getString("countryService.port"));
    bindConstant().annotatedWith(Names.named("countryServiceTimeout"))
        .to(configuration.getString("countryService.timeout"));

    // ogelService
    bindConstant().annotatedWith(Names.named("ogelServiceHost"))
        .to(configuration.getString("ogelService.hostname"));
    bindConstant().annotatedWith(Names.named("ogelServicePort"))
        .to(configuration.getString("ogelService.port"));
    bindConstant().annotatedWith(Names.named("ogelServiceTimeout"))
        .to(configuration.getString("ogelService.timeout"));

  }

  @Provides
  public JourneyManager provideJourneyManager () {
    return new JourneyManager(new JourneyDefinitionBuilder().build("dummy", null));
  }

}