package modules;

import static components.common.journey.JourneyDefinitionBuilder.moveTo;
import static play.mvc.Results.redirect;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import components.common.CommonGuiceModule;
import components.common.journey.JourneyDefinitionBuilder;
import components.common.journey.JourneyManager;
import components.common.journey.JourneyStage;
import components.common.journey.StandardEvents;
import components.common.state.ContextParamManager;
import controllers.routes;
import journey.Events;
import play.Configuration;
import play.Environment;

import java.util.concurrent.CompletableFuture;

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
  @Singleton
  public JourneyManager provideJourneyManager (ContextParamManager cpm) {

    JourneyDefinitionBuilder jdb = new JourneyDefinitionBuilder();

    JourneyStage index = jdb.defineStage("index", "Import / export licensing service",
        () -> CompletableFuture.completedFuture(redirect(routes.EntryPointController.index())));

    JourneyStage startApplication = jdb.defineStage("startApplication", "Saving your application",
        () -> cpm.addParamsAndRedirect(controllers.routes.StartApplicationController.renderForm()));

    JourneyStage continueApplication = jdb.defineStage("continueApplication", "Return to your application",
        () -> cpm.addParamsAndRedirect(routes.ContinueApplicationController.renderForm()));

    JourneyStage tradeType = jdb.defineStage("tradeType", "Where are your items going?",
        () -> cpm.addParamsAndRedirect(routes.TradeTypeController.renderForm()));

    jdb.atStage(index)
        .onEvent(Events.START_APPLICATION)
        .then(moveTo(startApplication));

    jdb.atStage(index)
        .onEvent(Events.CONTINUE_APPLICATION)
        .then(moveTo(continueApplication));

    jdb.atStage(startApplication)
        .onEvent(StandardEvents.NEXT)
        .then(moveTo(tradeType));

    // TODO Convert to injected link in view
    jdb.atStage(continueApplication)
        .onEvent(Events.START_APPLICATION)
        .then(moveTo(startApplication));

    // TODO Restore to last position, requires JourneyManager persistence
    jdb.atStage(continueApplication)
        .onEvent(Events.APPLICATION_FOUND)
        .then(moveTo(tradeType));

    // TODO Add in additional screen catering for this condition IELS-606
//    jdb.atStage(continueApplication)
//        .onEvent(Events.APPLICATION_NOT_FOUND);

    return new JourneyManager(jdb.build("default", index));
  }

}