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
import models.cms.enums.QuestionType;
import models.cms.enums.StageAnswerOutcomeType;
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
        .setGoToStageAnswerOutcomeType(StageAnswerOutcomeType.TOO_COMPLEX)
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

  public Result createTestJourney() {
    localDefinitionDao.deleteAllLocalDefinitions();
    controlEntryDao.deleteAllControlEntries();
    stageAnswerDao.deleteAllStageAnswers();
    globalDefinitionDao.deleteAllGlobalDefinitions();
    noteDao.deleteAllNotes();
    stageDao.deleteAllStages();
    journeyDao.deleteAllJournies();

    Journey journey = new Journey().setJourneyName("test-journey");
    Long journeyId = journeyDao.insertJourney(journey);

    ControlEntry ml1 = createControlEntry(null, "ML1", "Smooth-bore weapons with a calibre of less than 20mm", "Smooth-bore weapons", false, false);
    ControlEntry ml1a = createControlEntry(ml1.getId(), "ML1a", "Rifles and combination guns, handguns and machine guns", "Rifles", false, false);
    ControlEntry ml1b = createControlEntry(ml1.getId(), "ML1b", "Smooth-bore weapons", "Smooth-bore weapons", false, false);
    ControlEntry ml1c = createControlEntry(ml1.getId(), "ML1c", "Weapons using caseless ammunition", null, false, false);

    Stage stage1 = createStage(journeyId, ml1.getId(), "Question for the first stage", "This is an explanatory note.",
        QuestionType.STANDARD, AnswerType.SELECT_ONE, null);
    Stage stage2 = createStage(journeyId, ml1a.getId(), null, null, QuestionType.STANDARD, AnswerType.SELECT_ONE, null);
    Stage stage3 = createStage(journeyId, ml1b.getId(), null, null, QuestionType.STANDARD, AnswerType.SELECT_ONE, null);
    Stage stage4 = createStage(journeyId, ml1c.getId(), null, null, QuestionType.STANDARD, AnswerType.SELECT_ONE, null);

    createStageAnswer(stage1.getId(), ml1a.getId(), null, stage2.getId(), null, 1, false);
    createStageAnswer(stage1.getId(), ml1b.getId(), null, stage3.getId(), null, 1, false);
    createStageAnswer(stage1.getId(), ml1c.getId(), null, stage4.getId(), null, 1, false);

    createNote(stage1.getId(), "Stage 1 technical note", NoteType.TECH_NOTE);
    createNote(stage1.getId(), "Stage 1 NB note", NoteType.NB);

    return ok("DONE");
  }

  private ControlEntry createControlEntry(Long parentId, String controlCode, String fullDesc,
                                          String summaryDesc, boolean nested, boolean selectable) {
    ControlEntry controlEntry = new ControlEntry()
        .setParentControlEntryId(parentId)
        .setControlCode(controlCode)
        .setFullDescription(fullDesc)
        .setSummaryDescription(summaryDesc)
        .setNested(nested)
        .setSelectable(selectable)
        .setRegime("A regime");
    Long id = controlEntryDao.insertControlEntry(controlEntry);
    controlEntry.setId(id); //ID is generated by the database
    return controlEntry;
  }

  private Stage createStage(long journeyId, Long controlEntryId, String title, String explanatoryNotes,
                            QuestionType questionType, AnswerType answerType,
                            Long nextStageId) {
    Stage stage = new Stage()
        .setJourneyId(journeyId)
        .setControlEntryId(controlEntryId)
        .setTitle(title)
        .setExplanatoryNotes(explanatoryNotes)
        .setNextStageId(nextStageId)
        .setQuestionType(questionType)
        .setAnswerType(answerType);
    Long id = stageDao.insertStage(stage);
    stage.setId(id); //ID is generated by the database
    return stage;
  }

  private StageAnswer createStageAnswer(long parentStageId, Long controlEntryId, String answerText, Long nextStageId,
                                        StageAnswerOutcomeType stageAnswerOutcomeType, int displayOrder, boolean dividerAbove) {
    StageAnswer stageAnswer = new StageAnswer()
        .setParentStageId(parentStageId)
        .setControlEntryId(controlEntryId)
        .setAnswerText(answerText)
        .setGoToStageId(nextStageId)
        .setGoToStageAnswerOutcomeType(stageAnswerOutcomeType)
        .setDisplayOrder(displayOrder)
        .setDividerAbove(dividerAbove);
    Long id = stageAnswerDao.insertStageAnswer(stageAnswer);
    stageAnswer.setId(id); //ID is generated by the database
    return stageAnswer;
  }

  private Note createNote(long stageId, String noteText, NoteType noteType) {
    Note note = new Note()
        .setStageId(stageId)
        .setNoteText(noteText)
        .setNoteType(noteType);
    Long id = noteDao.insertNote(note);
    note.setId(id);  //ID is generated by the database
    return note;
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
