package triage.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.NoteDao;
import components.cms.dao.StageAnswerDao;
import components.cms.dao.StageDao;
import models.cms.ControlEntry;
import models.cms.Note;
import models.cms.Stage;
import models.cms.StageAnswer;
import org.apache.commons.lang.StringUtils;
import triage.config.AnswerConfig;
import triage.config.ControlEntryConfig;
import triage.config.NoteConfig;
import triage.config.OutcomeType;
import triage.config.StageConfig;
import triage.text.RichText;
import triage.text.RichTextParser;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JourneyConfigCacheImpl implements JourneyConfigCache {

  private final StageDao stageDao;
  private final StageAnswerDao stageAnswerDao;
  private final ControlEntryDao controlEntryDao;
  private final NoteDao noteDao;
  private final RichTextParser richTextParser;

  private final LoadingCache<String, StageConfig> stageConfigCache =
      CacheBuilder.newBuilder().build(CacheLoader.from(this::createStageConfigForId));
  private final LoadingCache<String, ControlEntryConfig> controlEntryCache =
      CacheBuilder.newBuilder().build(CacheLoader.from(this::createControlEntryConfigForId));
  private final LoadingCache<String, List<NoteConfig>> noteCache =
      CacheBuilder.newBuilder().build(CacheLoader.from(this::createNoteConfigsForStageId));

  @Inject
  public JourneyConfigCacheImpl(StageDao stageDao, StageAnswerDao stageAnswerDao,
                                ControlEntryDao controlEntryDao, NoteDao noteDao, RichTextParser richTextParser) {
    this.stageDao = stageDao;
    this.stageAnswerDao = stageAnswerDao;
    this.controlEntryDao = controlEntryDao;
    this.noteDao = noteDao;
    this.richTextParser = richTextParser;
  }

  @Override
  public StageConfig getStageConfigById(String stageId) {
    return stageConfigCache.getUnchecked(stageId);
  }

  @Override
  public List<NoteConfig> getNoteConfigsByStageId(String stageId) {
    return noteCache.getUnchecked(stageId);
  }

  @Override
  public ControlEntryConfig getControlEntryConfigById(String controlEntryId) {
    return controlEntryCache.getUnchecked(controlEntryId);
  }

  @Override
  public void flushCache() {
    stageConfigCache.invalidateAll();
    controlEntryCache.invalidateAll();
    noteCache.invalidateAll();

    stageConfigCache.cleanUp();
    controlEntryCache.cleanUp();
    noteCache.cleanUp();
  }

  private StageConfig createStageConfigForId(String stageId) {
    Stage stage = stageDao.getStage(Long.parseLong(stageId));
    if (stage == null) {
      return null;
    } else {
      return createStageConfig(stage);
    }
  }

  private ControlEntryConfig createControlEntryConfigForId(String controlEntryId) {
    return createControlEntryConfig(controlEntryDao.getControlEntry(Long.parseLong(controlEntryId)));
  }

  private List<NoteConfig> createNoteConfigsForStageId(String stageId) {
    return noteDao.getNotesForStageId(Long.parseLong(stageId))
        .stream()
        .map(this::createNoteConfig)
        .collect(Collectors.toList());
  }

  private StageConfig createStageConfig(Stage stage) {
    StageConfig.QuestionType questionType = null;
    switch (stage.getQuestionType()) {
      case STANDARD:
        questionType = StageConfig.QuestionType.STANDARD;
        break;
      case DECONTROL:
        questionType = StageConfig.QuestionType.DECONTROL;
        break;
    }

    StageConfig.AnswerType answerType = null;
    switch (stage.getAnswerType()) {
      case SELECT_ONE:
        answerType = StageConfig.AnswerType.SELECT_ONE;
        break;
      case SELECT_MANY:
        answerType = StageConfig.AnswerType.SELECT_MANY;
        break;
    }

    OutcomeType stageOutcomeType = null;
    if (stage.getStageOutcomeType() != null) {
      switch (stage.getStageOutcomeType()) {
        case CONTROL_ENTRY_FOUND:
          stageOutcomeType = OutcomeType.CONTROL_ENTRY_FOUND;
          break;
      }
    }

    RichText explanatoryNote = richTextParser.parseForStage(StringUtils.defaultString(stage.getExplanatoryNotes()),
        Long.toString(stage.getId()));
    String nextStageId = Optional.ofNullable(stage.getNextStageId()).map(Object::toString).orElse(null);
    ControlEntryConfig controlEntryConfig = Optional.ofNullable(stage.getControlEntryId())
        .map(controlEntryDao::getControlEntry)
        .map(this::createControlEntryConfig)
        .orElse(null);

    List<AnswerConfig> answerConfigs = stageAnswerDao.getStageAnswersForStageId(stage.getId())
        .stream()
        .map(this::createAnswerConfig)
        .sorted(Comparator.comparing(AnswerConfig::getDisplayOrder))
        .collect(Collectors.toList());

    return new StageConfig(Long.toString(stage.getId()), stage.getTitle(), explanatoryNote, questionType, answerType,
        nextStageId, stageOutcomeType, controlEntryConfig, answerConfigs);
  }

  private AnswerConfig createAnswerConfig(StageAnswer stageAnswer) {

    String nextStageId = Optional.ofNullable(stageAnswer.getGoToStageId()).map(Object::toString).orElse(null);
    OutcomeType outcomeType = null;
    if (stageAnswer.getGoToStageAnswerOutcomeType() != null) {
      switch (stageAnswer.getGoToStageAnswerOutcomeType()) {
        case CONTROL_ENTRY_FOUND:
          outcomeType = OutcomeType.CONTROL_ENTRY_FOUND;
          break;
        case DECONTROL:
          outcomeType = OutcomeType.DECONTROL;
          break;
        case TOO_COMPLEX:
          outcomeType = OutcomeType.TOO_COMPLEX;
          break;
      }
    }

    RichText labelText = Optional.ofNullable(stageAnswer.getAnswerText())
        .map(e -> richTextParser.parseForStage(e, nextStageId)).orElse(null);
    RichText nestedContent = Optional.ofNullable(stageAnswer.getNestedContent())
        .map(e -> richTextParser.parseForStage(e, nextStageId)).orElse(null);
    RichText moreInfoContent = Optional.ofNullable(stageAnswer.getMoreInfoContent())
        .map(e -> richTextParser.parseForStage(e, nextStageId)).orElse(null);

    ControlEntryConfig controlEntryConfig = Optional.ofNullable(stageAnswer.getControlEntryId())
        .map(controlEntryDao::getControlEntry)
        .map(this::createControlEntryConfig)
        .orElse(null);

    Integer answerPrecedence = Optional.ofNullable(stageAnswer.getAnswerPrecedence()).orElse(
        stageAnswer.getDisplayOrder());

    return new AnswerConfig(stageAnswer.getId().toString(), nextStageId, outcomeType, labelText, nestedContent,
        moreInfoContent, controlEntryConfig, stageAnswer.getDisplayOrder(), answerPrecedence,
        stageAnswer.isDividerAbove());
  }

  private ControlEntryConfig createControlEntryConfig(ControlEntry controlEntry) {
    String controlEntryId = controlEntry.getId().toString();
    RichText fullDescription = richTextParser.parseForControlEntry(controlEntry.getFullDescription(), controlEntryId);
    String summaryDescriptionString = StringUtils.defaultString(controlEntry.getSummaryDescription());
    RichText summaryDescription = richTextParser.parseForControlEntry(summaryDescriptionString, controlEntryId);

    ControlEntryConfig parentControlEntryConfig = null;
    if (controlEntry.getParentControlEntryId() != null) {
      ControlEntry parentControlEntry = controlEntryDao.getControlEntry(controlEntry.getParentControlEntryId());
      parentControlEntryConfig = createControlEntryConfig(parentControlEntry);
    }

    boolean hasNestedChildren = controlEntryDao.getChildControlEntries(controlEntry.getId())
        .stream()
        .anyMatch(ControlEntry::isNested);

    return new ControlEntryConfig(Long.toString(controlEntry.getId()), controlEntry.getControlCode(), fullDescription,
        summaryDescription, parentControlEntryConfig, hasNestedChildren, controlEntry.isSelectable());
  }

  private NoteConfig createNoteConfig(Note note) {
    String stageId = note.getStageId().toString();
    RichText noteText = richTextParser.parseForStage(note.getNoteText(), stageId);

    NoteConfig.NoteType noteType = null;
    switch (note.getNoteType()) {
      case NB:
        noteType = NoteConfig.NoteType.NB;
        break;
      case NOTE:
        noteType = NoteConfig.NoteType.NOTE;
        break;
      case SEE_ALSO:
        noteType = NoteConfig.NoteType.SEE_ALSO;
        break;
      case TECH_NOTE:
        noteType = NoteConfig.NoteType.TECHNICAL_NOTE;
        break;
    }

    return new NoteConfig(note.getId().toString(), stageId, noteText, noteType);
  }

}
