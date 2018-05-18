package triage.config;

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
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import triage.text.RichText;
import triage.text.RichTextParser;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class JourneyConfigServiceDaoImpl implements JourneyConfigService {

  private final StageDao stageDao;
  private final StageAnswerDao stageAnswerDao;
  private final ControlEntryDao controlEntryDao;
  private final NoteDao noteDao;
  private final RichTextParser richTextParser;
  private final DBI dbi;

  @Inject
  public JourneyConfigServiceDaoImpl(StageDao stageDao, StageAnswerDao stageAnswerDao, ControlEntryDao controlEntryDao,
                                     NoteDao noteDao, RichTextParser richTextParser, DBI dbi) {
    this.stageDao = stageDao;
    this.stageAnswerDao = stageAnswerDao;
    this.controlEntryDao = controlEntryDao;
    this.noteDao = noteDao;
    this.richTextParser = richTextParser;
    this.dbi = dbi;
  }

  @Override
  public String getInitialStageId() {
    //TODO fix the data model so a journey knows its initial stage ID - this can then just use the journey DAO
    try(Handle handle = dbi.open()) {
      List<Map<String, Object>> select = handle.select("SELECT s.id\n" +
          "FROM stage s\n" +
          "LEFT JOIN stage_answer sa ON s.id = sa.go_to_stage_id\n" +
          "WHERE sa.id IS NULL");
      return select.get(0).get("id").toString();
    }
  }

  @Override
  public StageConfig getStageConfigForStageId(String stageId) {

    Stage stage = stageDao.getStage(Long.parseLong(stageId));

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

    RichText explanatoryNote = richTextParser.parse(StringUtils.defaultString(stage.getExplanatoryNotes()), stageId);
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
        nextStageId, controlEntryConfig, answerConfigs);
  }

  private AnswerConfig createAnswerConfig(StageAnswer stageAnswer) {

    String nextStageId = Optional.ofNullable(stageAnswer.getGoToStageId()).map(Object::toString).orElse(null);
    AnswerConfig.OutcomeType outcomeType = null;
    if (stageAnswer.getGoToOutcomeType() != null) {
      switch (stageAnswer.getGoToOutcomeType()) {
        case CONTROL_ENTRY_FOUND:
          outcomeType = AnswerConfig.OutcomeType.CONTROL_ENTRY_FOUND;
          break;
        case DECONTROL:
          outcomeType = AnswerConfig.OutcomeType.DECONTROL;
          break;
        case TOO_COMPLEX:
          outcomeType = AnswerConfig.OutcomeType.TOO_COMPLEX;
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
  public List<NoteConfig> getNotesForStageId(String stageId) {
    return noteDao.getNotesForStageId(Long.parseLong(stageId))
        .stream()
        .map(this::createNoteConfig)
        .collect(Collectors.toList());
  }

  @Override
  public ControlEntryConfig getControlEntryConfigForId(String controlEntryId) {
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
