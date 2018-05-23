package triage.config;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.JourneyDao;
import components.cms.dao.NoteDao;
import components.cms.dao.StageAnswerDao;
import components.cms.dao.StageDao;
import models.cms.ControlEntry;
import models.cms.Journey;
import models.cms.Note;
import models.cms.Stage;
import models.cms.StageAnswer;
import org.apache.commons.lang.StringUtils;
import triage.text.RichText;
import triage.text.RichTextParser;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JourneyConfigServiceDaoImpl implements JourneyConfigService {

  private final JourneyDao journeyDao;
  private final StageDao stageDao;
  private final StageAnswerDao stageAnswerDao;
  private final ControlEntryDao controlEntryDao;
  private final NoteDao noteDao;
  private final RichTextParser richTextParser;

  @Inject
  public JourneyConfigServiceDaoImpl(JourneyDao journeyDao, StageDao stageDao, StageAnswerDao stageAnswerDao,
                                     ControlEntryDao controlEntryDao, NoteDao noteDao, RichTextParser richTextParser) {
    this.journeyDao = journeyDao;
    this.stageDao = stageDao;
    this.stageAnswerDao = stageAnswerDao;
    this.controlEntryDao = controlEntryDao;
    this.noteDao = noteDao;
    this.richTextParser = richTextParser;
  }

  @Override
  public String getInitialStageId() {
    return journeyDao.getJourneysByJourneyName("MILITARY").stream()
        .map(Journey::getInitialStageId)
        .map(Object::toString)
        .findFirst()
        .orElse(null);
  }

  @Override
  public StageConfig getStageConfigById(String stageId) {
    Stage stage = stageDao.getStage(Long.parseLong(stageId));
    if (stage == null) {
      return null;
    } else {
      return createStageConfig(stage);
    }
  }

  @Override
  public AnswerConfig getStageAnswerForPreviousStage(String stageId) {
    StageAnswer stageAnswer = stageAnswerDao.getStageAnswerByGoToStageId(Long.parseLong(stageId));
    if (stageAnswer != null) {
      return createAnswerConfig(stageAnswer);
    } else {
      return null;
    }
  }

  @Override
  public StageConfig getStageConfigForPreviousStage(String stageId) {
    Stage stage = stageDao.getByNextStageId(Long.parseLong(stageId));
    if (stage == null) {
      StageAnswer stageAnswer = stageAnswerDao.getStageAnswerByGoToStageId(Long.parseLong(stageId));
      if (stageAnswer != null) {
        return createStageConfig(stageDao.getStage(stageAnswer.getParentStageId()));
      } else {
        return null;
      }
    } else {
      return createStageConfig(stage);
    }
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

    RichText explanatoryNote = richTextParser.parse(StringUtils.defaultString(stage.getExplanatoryNotes()), Long.toString(stage.getId()));
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
        .map(e -> richTextParser.parse(e, nextStageId)).orElse(null);
    RichText nestedContent = Optional.ofNullable(stageAnswer.getNestedContent())
        .map(e -> richTextParser.parse(e, nextStageId)).orElse(null);
    RichText moreInfoContent = Optional.ofNullable(stageAnswer.getMoreInfoContent())
        .map(e -> richTextParser.parse(e, nextStageId)).orElse(null);

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
    String stageId = null; //TODO determine stage ID for code

    RichText fullDescription = richTextParser.parse(controlEntry.getFullDescription(), stageId);
    RichText summaryDescription = richTextParser.parse(StringUtils.defaultString(controlEntry.getSummaryDescription()),
        stageId);

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

  @Override
  public List<NoteConfig> getNoteConfigsByStageId(String stageId) {
    return noteDao.getNotesForStageId(Long.parseLong(stageId))
        .stream()
        .map(this::createNoteConfig)
        .collect(Collectors.toList());
  }

  @Override
  public ControlEntryConfig getControlEntryConfigById(String controlEntryId) {
    return createControlEntryConfig(controlEntryDao.getControlEntry(Long.parseLong(controlEntryId)));
  }

  private NoteConfig createNoteConfig(Note note) {
    String stageId = note.getStageId().toString();
    RichText noteText = richTextParser.parse(note.getNoteText(), stageId);

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

  @Override
  public List<String> getStageIdsForControlEntry(ControlEntryConfig controlEntryConfig) {
    return stageDao.getStagesForControlEntryId(Long.parseLong(controlEntryConfig.getId()))
        .stream()
        .map(e -> e.getId().toString())
        .collect(Collectors.toList());
  }

  @Override
  public List<ControlEntryConfig> getChildRatings(ControlEntryConfig controlEntryConfig) {
    return controlEntryDao
        .getChildControlEntries(Long.parseLong(controlEntryConfig.getId()))
        .stream()
        .map(this::createControlEntryConfig)
        .collect(Collectors.toList());
  }
}
