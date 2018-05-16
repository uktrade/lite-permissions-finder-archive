package modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import components.common.persistence.RedisKeyConfig;
import components.common.persistence.StatelessRedisDao;
import org.redisson.api.RedissonClient;

public class RedisDaoGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
  }

  /*

  @Provides
  @Named("adminApprovalStateless")
  public StatelessRedisDao provideAdminApprovalStateless(@Named("adminApproval") RedisKeyConfig redisKeyConfig,
                                                         RedissonClient redissonClient) {
    return new StatelessRedisDao(redisKeyConfig, redissonClient);
  }*/

  @Provides
  @Named("ogelRegistrationStateless")
  public StatelessRedisDao provideOgelRegistrationStateless(@Named("ogelRegistration") RedisKeyConfig keyConfig,
                                                            RedissonClient redissonClient) {
    return new StatelessRedisDao(keyConfig, redissonClient);
  }

  @Provides
  @Named("registrationSubmissionStateless")
  public StatelessRedisDao provideRegistrationSubmissionStateless(
      @Named("registrationSubmission") RedisKeyConfig keyConfig, RedissonClient redissonClient) {
    return new StatelessRedisDao(keyConfig, redissonClient);
  }

  @Provides
  @Named("registrationTransactionStateless")
  public StatelessRedisDao provideRegistrationTransactionStateless(
      @Named("registrationTransaction") RedisKeyConfig keyConfig, RedissonClient redissonClient) {
    return new StatelessRedisDao(keyConfig, redissonClient);
  }

}
