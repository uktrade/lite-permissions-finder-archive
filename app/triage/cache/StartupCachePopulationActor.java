package triage.cache;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class StartupCachePopulationActor {

  private final ActorSystem actorSystem;
  private final ExecutionContext executionContext;
  private final CachePopulationService cachePopulationService;

  @Inject
  public StartupCachePopulationActor(ActorSystem actorSystem, ExecutionContext executionContext,
                                     CachePopulationService cachePopulationService) {
    this.actorSystem = actorSystem;
    this.executionContext = executionContext;
    this.cachePopulationService = cachePopulationService;

    this.initialize();
  }

  private void initialize() {
    this.actorSystem.scheduler().scheduleOnce(
        Duration.create(0, TimeUnit.SECONDS),
        cachePopulationService::populateCache,
        this.executionContext
    );
  }
}
