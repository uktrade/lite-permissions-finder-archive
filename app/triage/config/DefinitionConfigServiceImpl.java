package triage.config;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.GlobalDefinitionDao;
import components.cms.dao.LocalDefinitionDao;
import models.cms.GlobalDefinition;
import models.cms.LocalDefinition;
import triage.text.RichText;
import triage.text.RichTextParser;

public class DefinitionConfigServiceImpl implements DefinitionConfigService {

  private final RichTextParser richTextParser;
  private final GlobalDefinitionDao globalDefinitionDao;
  private final LocalDefinitionDao localDefinitionDao;
  private final ControlEntryDao controlEntryDao;

  private final LoadingCache<String, DefinitionConfig> globalDefinitionCache;
  private final LoadingCache<String, DefinitionConfig> localDefinitionCache;


  @Inject
  public DefinitionConfigServiceImpl(GlobalDefinitionDao globalDefinitionDao, LocalDefinitionDao localDefinitionDao,
                                     ControlEntryDao controlEntryDao, RichTextParser richTextParser) {
    this.richTextParser = richTextParser;
    this.localDefinitionDao = localDefinitionDao;
    this.globalDefinitionDao = globalDefinitionDao;
    this.controlEntryDao = controlEntryDao;
    this.globalDefinitionCache = CacheBuilder.newBuilder().build(CacheLoader.from(this::createGlobalDefinition));
    this.localDefinitionCache = CacheBuilder.newBuilder().build(CacheLoader.from(this::createLocalDefinition));
  }

  @Override
  public DefinitionConfig getGlobalDefinition(String id) {
    return globalDefinitionCache.getUnchecked(id);
  }

  @Override
  public DefinitionConfig getLocalDefinition(String id) {
    return localDefinitionCache.getUnchecked(id);
  }

  private DefinitionConfig createGlobalDefinition(String id) {
    GlobalDefinition globalDefinition = globalDefinitionDao.getGlobalDefinition(Long.parseLong(id));
    RichText richDefinitionText = richTextParser.parseForStage(globalDefinition.getDefinitionText(), null, Long.toString(globalDefinition.getJourneyId()));
    return new DefinitionConfig(Long.toString(globalDefinition.getId()), globalDefinition.getTerm(), richDefinitionText, null);
  }

  private DefinitionConfig createLocalDefinition(String id) {
    LocalDefinition localDefinition = localDefinitionDao.getLocalDefinition(Long.parseLong(id));
    Long controlEntryId = localDefinition.getControlEntryId();
    RichText richDefinitionText = richTextParser.parseForControlEntry(localDefinition.getDefinitionText(), Long.toString(controlEntryId), Long.toString(controlEntryDao.getControlEntry(controlEntryId).getJourneyId()));
    return new DefinitionConfig(Long.toString(localDefinition.getId()), localDefinition.getTerm(), richDefinitionText, null);
  }

  @Override
  public void flushCache() {
    globalDefinitionCache.invalidateAll();
    localDefinitionCache.invalidateAll();

    globalDefinitionCache.cleanUp();
    localDefinitionCache.cleanUp();
  }

}
