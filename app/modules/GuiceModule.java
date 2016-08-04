package modules;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
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
    bindConstant().annotatedWith(Names.named("controlCodeSearchServiceHostname"))
        .to(configuration.getString("controlCodeSearchService.hostname"));
    bindConstant().annotatedWith(Names.named("controlCodeFrontendServiceHostname"))
        .to(configuration.getString("controlCodeFrontendService.hostname"));
  }
}