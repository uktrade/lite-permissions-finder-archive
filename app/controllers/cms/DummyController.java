package controllers.cms;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.GlobalDefinitionDao;
import components.cms.dao.JourneyDao;
import components.cms.dao.LocalDefinitionDao;
import components.cms.dao.NoteDao;
import components.cms.dao.StageAnswerDao;
import components.cms.dao.StageDao;
import models.cms.ControlEntry;
import models.cms.GlobalDefinition;
import models.cms.Journey;
import models.cms.LocalDefinition;
import models.cms.Note;
import models.cms.Stage;
import models.cms.StageAnswer;
import models.cms.enums.AnswerType;
import models.cms.enums.NoteType;
import models.cms.enums.OutcomeType;
import models.cms.enums.QuestionType;
import play.mvc.Result;

public class DummyController {

  private final ControlEntryDao controlEntryDao;
  private final GlobalDefinitionDao globalDefinitionDao;
  private final JourneyDao journeyDao;
  private final LocalDefinitionDao localDefinitionDao;
  private final NoteDao noteDao;
  private final StageAnswerDao stageAnswerDao;
  private final StageDao stageDao;

  @Inject
  public DummyController(
      ControlEntryDao controlEntryDao,
      GlobalDefinitionDao globalDefinitionDao,
      JourneyDao journeyDao,
      LocalDefinitionDao localDefinitionDao,
      NoteDao noteDao,
      StageAnswerDao stageAnswerDao,
      StageDao stageDao) {
    this.controlEntryDao = controlEntryDao;
    this.globalDefinitionDao = globalDefinitionDao;
    this.journeyDao = journeyDao;
    this.localDefinitionDao = localDefinitionDao;
    this.noteDao = noteDao;
    this.stageAnswerDao = stageAnswerDao;
    this.stageDao = stageDao;
  }

  public Result testAll() {
    localDefinitionDao.deleteAllLocalDefinitions();
    controlEntryDao.deleteAllControlEntries();
    stageAnswerDao.deleteAllStageAnswers();
    globalDefinitionDao.deleteAllGlobalDefinitions();
    noteDao.deleteAllNotes();
    stageDao.deleteAllStages();
    journeyDao.deleteAllJournies();

    Journey journey = new Journey().setJourneyName("test-journey");
    Long journeyId = journeyDao.insertJourney(journey);

    Stage stage = new Stage()
        .setJourneyId(journeyId)
        .setQuestionType(QuestionType.STANDARD)
        .setAnswerType(AnswerType.SELECT_MANY);
    Long stageId = stageDao.insertStage(stage);

    StageAnswer stageAnswer = new StageAnswer()
        .setParentStageId(stageId)
        .setGoToOutcomeType(OutcomeType.TOO_COMPLEX)
        .setDisplayOrder(1)
        .setDividerAbove(false)
        .setAnswerText("Some answer test");
    Long stageAnswerId = stageAnswerDao.insertStageAnswer(stageAnswer);

    ControlEntry controlEntry = new ControlEntry()
        .setControlCode("ML1a")
        .setFullDescription("A full description")
        .setNested(false)
        .setSelectable(false);
    Long controlEntryId = controlEntryDao.insertControlEntry(controlEntry);

    GlobalDefinition globalDefinition = new GlobalDefinition()
        .setJourneyId(journeyId)
        .setTerm("A term")
        .setDefinitionText("Some definition text");
    Long globalDefinitionId = globalDefinitionDao.insertGlobalDefinition(globalDefinition);

    LocalDefinition localDefinition = new LocalDefinition()
        .setControlEntryId(controlEntryId)
        .setTerm("A term")
        .setDefinitionText("Some definition text");
    Long localDefinitionId = localDefinitionDao.insertLocalDefinition(localDefinition);

    Note note = new Note()
        .setStageId(stageId)
        .setNoteText("Some note text")
        .setNoteType(NoteType.TECH_NOTE);
    Long noteId = noteDao.insertNote(note);

    journey = journeyDao.getJourney(journeyId);
    stage = stageDao.getStage(stageId);
    stageAnswer = stageAnswerDao.getStageAnswer(stageAnswerId);
    controlEntry = controlEntryDao.getControlEntry(controlEntryId);
    globalDefinition = globalDefinitionDao.getGlobalDefinition(globalDefinitionId);
    localDefinition = localDefinitionDao.getLocalDefinition(localDefinitionId);
    note = noteDao.getNote(noteId);

    String message = String.format("journey id: %d, stage id: %d, stage answer id: %d, control entry id: %d, global definition id: %d, local definition id: %d, note id: %d", journey.getId(), stage.getId(), stageAnswer.getId(), controlEntry.getId(), globalDefinition.getId(), localDefinition.getId(), note.getId());
    return ok(message);
  }

  public Result insertControlEntry() {
    ControlEntry controlEntry = new ControlEntry()
        .setId(999999L)
        .setParentControlEntryId(1L)
        .setControlCode("ML1a")
        .setFullDescription("A full description")
        .setSummaryDescription("A summary description")
        .setNested(false)
        .setSelectable(false)
        .setRegime("A regime");
    Long id = controlEntryDao.insertControlEntry(controlEntry);
    return ok(id.toString());
  }

  public Result getControlEntry(String id) {
    return ok(controlEntryDao.getControlEntry(Long.parseLong(id)).getId().toString());
  }

  public Result deleteControlEntry(String id) {
    controlEntryDao.deleteControlEntry(Long.parseLong(id));
    return ok("deleted");
  }

  public Result deleteAllControlEntires() {
    controlEntryDao.deleteAllControlEntries();
    return ok("deleted all control entries");
  }
}
