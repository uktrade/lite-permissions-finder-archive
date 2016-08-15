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

    bindConstant().annotatedWith(Names.named("controlCodeSearchServiceHostname"))
        .to(configuration.getString("controlCodeSearchService.hostname"));
    bindConstant().annotatedWith(Names.named("controlCodeFrontendServiceHostname"))
        .to(configuration.getString("controlCodeFrontendService.hostname"));

  }

  @Provides
  public JourneyManager provideJourneyManager () {
    return new JourneyManager(new JourneyDefinitionBuilder().build("dummy", null));
  }

}