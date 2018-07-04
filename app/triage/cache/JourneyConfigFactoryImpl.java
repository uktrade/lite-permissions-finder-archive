package triage.cache;

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
import triage.config.StageConfig;
import triage.text.RichText;
import triage.text.RichTextParser;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JourneyConfigFactoryImpl implements JourneyConfigFactory {

  private final StageDao stageDao;
  private final RichTextParser richTextParser;
  private final ControlEntryDao controlEntryDao;
  private final StageAnswerDao stageAnswerDao;
  private final NoteDao noteDao;

  @Inject
  public JourneyConfigFactoryImpl(StageDao stageDao, RichTextParser richTextParser,
                                  ControlEntryDao controlEntryDao, StageAnswerDao stageAnswerDao,
                                  NoteDao noteDao) {
    this.stageDao = stageDao;
    this.richTextParser = richTextParser;
    this.controlEntryDao = controlEntryDao;
    this.stageAnswerDao = stageAnswerDao;
    this.noteDao = noteDao;
  }

  @Override
  public Optional<StageConfig> createStageConfigForId(String stageId) {
    long id;
    try {
      id = Long.parseLong(stageId);
    } catch (NumberFormatException nfe) {
      return Optional.empty();
    }
    return Optional.ofNullable(stageDao.getStage(id))
        .map(this::createStageConfig);
  }

  @Override
  public Optional<ControlEntryConfig> createControlEntryConfigForId(String controlEntryId) {
    long id;
    try {
      id = Long.parseLong(controlEntryId);
    } catch (NumberFormatException nfe) {
      return Optional.empty();
    }
    return Optional.ofNullable(controlEntryDao.getControlEntry(id))
        .map(this::createControlEntryConfig);
  }

  @Override
  public List<ControlEntryConfig> createRelatedControlEntryConfigsForId(String controlEntryId) {
    return controlEntryDao.getRelatedControlCodeEntries(Long.parseLong(controlEntryId))
        .stream()
        .map(this::createControlEntryConfig)
        .collect(Collectors.toList());
  }

  @Override
  public List<NoteConfig> createNoteConfigsForStageId(String stageId) {
    return noteDao.getNotesForStageId(Long.parseLong(stageId))
        .stream()
        .map(this::createNoteConfig)
        .collect(Collectors.toList());
  }

  private StageConfig createStageConfig(Stage stage) {
    String journeyId = Long.toString(stage.getJourneyId());
    RichText explanatoryNote = richTextParser.parseForStage(StringUtils.defaultString(stage.getExplanatoryNotes()), journeyId);
    String nextStageId = Optional.ofNullable(stage.getNextStageId()).map(Object::toString).orElse(null);
    ControlEntryConfig controlEntryConfig = Optional.ofNullable(stage.getControlEntryId())
        .map(controlEntryDao::getControlEntry)
        .map(this::createControlEntryConfig)
        .orElse(null);

    List<AnswerConfig> answerConfigs = stageAnswerDao.getStageAnswersForStageId(stage.getId())
        .stream()
        .map(stageAnswer -> createAnswerConfig(stageAnswer, journeyId))
        .sorted(Comparator.comparing(AnswerConfig::getDisplayOrder))
        .collect(Collectors.toList());

    return new StageConfig(Long.toString(stage.getId()), stage.getTitle(), explanatoryNote, stage.getQuestionType(),
        stage.getAnswerType(), nextStageId, stage.getStageOutcomeType(), controlEntryConfig, answerConfigs);
  }

  private AnswerConfig createAnswerConfig(StageAnswer stageAnswer, String journeyId) {

    String nextStageId = Optional.ofNullable(stageAnswer.getGoToStageId()).map(Object::toString).orElse(null);

    RichText labelText = Optional.ofNullable(stageAnswer.getAnswerText())
        .map(e -> richTextParser.parseForStage(e, journeyId)).orElse(null);
    RichText nestedContent = Optional.ofNullable(stageAnswer.getNestedContent())
        .map(e -> richTextParser.parseForStage(e, journeyId)).orElse(null);
    RichText moreInfoContent = Optional.ofNullable(stageAnswer.getMoreInfoContent())
        .map(e -> richTextParser.parseForStage(e, journeyId)).orElse(null);

    ControlEntryConfig controlEntryConfig = Optional.ofNullable(stageAnswer.getControlEntryId())
        .map(controlEntryDao::getControlEntry)
        .map(this::createControlEntryConfig)
        .orElse(null);

    Integer answerPrecedence = Optional.ofNullable(stageAnswer.getAnswerPrecedence()).orElse(
        stageAnswer.getDisplayOrder());

    return new AnswerConfig(stageAnswer.getId().toString(), nextStageId, stageAnswer.getGoToOutcomeType(), labelText, nestedContent,
        moreInfoContent, controlEntryConfig, stageAnswer.getDisplayOrder(), answerPrecedence,
        stageAnswer.isDividerAbove());
  }

  private ControlEntryConfig createControlEntryConfig(ControlEntry controlEntry) {
    String controlEntryId = controlEntry.getId().toString();
    RichText fullDescription = richTextParser.parseForControlEntry(controlEntry.getFullDescription(), controlEntryId,
        Long.toString(controlEntry.getJourneyId()));
    String summaryDescriptionString = StringUtils.defaultString(controlEntry.getSummaryDescription());
    RichText summaryDescription = richTextParser.parseForControlEntry(summaryDescriptionString, controlEntryId,
        Long.toString(controlEntry.getJourneyId()));

    ControlEntryConfig parentControlEntryConfig = null;
    if (controlEntry.getParentControlEntryId() != null) {
      ControlEntry parentControlEntry = controlEntryDao.getControlEntry(controlEntry.getParentControlEntryId());
      parentControlEntryConfig = createControlEntryConfig(parentControlEntry);
    }

    boolean hasNestedChildren = controlEntryDao.getChildControlEntries(controlEntry.getId())
        .stream()
        .anyMatch(ControlEntry::isNested);

    return new ControlEntryConfig(Long.toString(controlEntry.getId()), controlEntry.getControlCode(), fullDescription,
        summaryDescription, parentControlEntryConfig, hasNestedChildren);
  }

  private NoteConfig createNoteConfig(Note note) {
    String stageId = note.getStageId().toString();
    Long journeyId = Optional.ofNullable(stageDao.getStage(Long.parseLong(stageId))).map(Stage::getJourneyId).orElse(null);
    RichText noteText = richTextParser.parseForStage(note.getNoteText(), Long.toString(journeyId));
    return new NoteConfig(note.getId().toString(), stageId, noteText, note.getNoteType());
  }

}
