package triage.config;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import components.cms.dao.GlobalDefinitionDao;
import components.cms.dao.LocalDefinitionDao;
import models.cms.GlobalDefinition;
import models.cms.LocalDefinition;
import triage.text.RichText;
import triage.text.RichTextParser;

import java.util.Optional;

public class DefinitionConfigServiceImpl implements DefinitionConfigService {

  private final RichTextParser richTextParser;
  private final GlobalDefinitionDao globalDefinitionDao;
  private final LocalDefinitionDao localDefinitionDao;

  private final LoadingCache<String, Optional<DefinitionConfig>> globalDefinitionCache;
  private final LoadingCache<String, Optional<DefinitionConfig>> localDefinitionCache;


  @Inject
  public DefinitionConfigServiceImpl(GlobalDefinitionDao globalDefinitionDao, LocalDefinitionDao localDefinitionDao,
                                     RichTextParser richTextParser) {
    this.richTextParser = richTextParser;
    this.localDefinitionDao = localDefinitionDao;
    this.globalDefinitionDao = globalDefinitionDao;
    this.globalDefinitionCache = CacheBuilder.newBuilder().build(CacheLoader.from(this::createGlobalDefinition));
    this.localDefinitionCache = CacheBuilder.newBuilder().build(CacheLoader.from(this::createLocalDefinition));
  }

  @Override
  public Optional<DefinitionConfig> getGlobalDefinition(String id) {
    return globalDefinitionCache.getUnchecked(id);
  }

  @Override
  public Optional<DefinitionConfig> getLocalDefinition(String id) {
    return localDefinitionCache.getUnchecked(id);
  }

  private Optional<DefinitionConfig> createGlobalDefinition(String definitionId) {
    long id;
    try {
      id = Long.parseLong(definitionId);
    } catch (NumberFormatException nfe) {
      return Optional.empty();
    }
    GlobalDefinition globalDefinition = globalDefinitionDao.getGlobalDefinition(id);
    if (globalDefinition == null) {
      return Optional.empty();
    } else {
      RichText richDefinitionText = richTextParser.parseForStage(globalDefinition.getDefinitionText(), null);
      DefinitionConfig definitionConfig = new DefinitionConfig(Long.toString(globalDefinition.getId()), globalDefinition.getTerm(), richDefinitionText, null);
      return Optional.of(definitionConfig);
    }
  }

  private Optional<DefinitionConfig> createLocalDefinition(String definitionId) {
    long id;
    try {
      id = Long.parseLong(definitionId);
    } catch (NumberFormatException nfe) {
      return Optional.empty();
    }
    LocalDefinition localDefinition = localDefinitionDao.getLocalDefinition(id);
    if (localDefinition == null) {
      return Optional.empty();
    } else {
      RichText richDefinitionText = richTextParser.parseForStage(localDefinition.getDefinitionText(), null);
      DefinitionConfig definitionConfig = new DefinitionConfig(Long.toString(localDefinition.getId()), localDefinition.getTerm(), richDefinitionText, null);
      return Optional.of(definitionConfig);
    }
  }

  @Override
  public void flushCache() {
    globalDefinitionCache.invalidateAll();
    localDefinitionCache.invalidateAll();

    globalDefinitionCache.cleanUp();
    localDefinitionCache.cleanUp();
  }

}
