package components.cms.loader;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.GlobalDefinitionDao;
import components.cms.dao.JourneyDao;
import components.cms.dao.LocalDefinitionDao;
import components.cms.dao.NoteDao;
import components.cms.dao.StageAnswerDao;
import components.cms.dao.StageDao;
import components.cms.parser.model.LoadingMetadata;
import components.cms.parser.model.NavigationLevel;
import components.cms.parser.model.column.Breadcrumbs;
import components.cms.parser.model.column.Buttons;
import components.cms.parser.model.column.ControlListEntries;
import components.cms.parser.model.column.Decontrols;
import components.cms.parser.model.column.Definitions;
import components.cms.parser.model.column.NavigationExtras;
import components.cms.parser.model.column.Notes;
import models.cms.ControlEntry;
import models.cms.Journey;
import models.cms.LocalDefinition;
import models.cms.Note;
import models.cms.Stage;
import models.cms.StageAnswer;
import models.cms.enums.AnswerType;
import models.cms.enums.NoteType;
import models.cms.enums.QuestionType;
import models.cms.enums.StageAnswerOutcomeType;
import models.cms.enums.StageOutcomeType;
import org.apache.commons.lang3.StringUtils;
import play.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Loader {
  private final ControlEntryDao controlEntryDao;
  private final GlobalDefinitionDao globalDefinitionDao;
  private final JourneyDao journeyDao;
  private final LocalDefinitionDao localDefinitionDao;
  private final NoteDao noteDao;
  private final StageAnswerDao stageAnswerDao;
  private final StageDao stageDao;

  @Inject
  public Loader(
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

  public void load(List<NavigationLevel> navigationLevels) {
    clearDown();
    NavigationLevel rootNavigationLevel = new NavigationLevel("ROOT", "ROOT", -1);
    rootNavigationLevel.addAllSubNavigationlevels(navigationLevels);
    Journey journey = new Journey().setJourneyName("MILITARY");
    Long journeyId = journeyDao.insertJourney(journey);
    createControlEntries(null, rootNavigationLevel);
    createStages(journeyId, rootNavigationLevel);
    createStageAnswersAndDecontrolStages(true, journeyId, 1, rootNavigationLevel);
    createLocalDefinitions(rootNavigationLevel);
    createNotes(rootNavigationLevel);
  }

  private void createControlEntries(Long parentControlEntryId, NavigationLevel navigationLevel) {
    Long controlEntryId = null;

    ControlListEntries controlListEntries = navigationLevel.getControlListEntries();
    if (controlListEntries != null && controlListEntries.getRating() != null) {
      ControlEntry controlEntry = new ControlEntry()
          .setParentControlEntryId(parentControlEntryId)
          .setFullDescription(navigationLevel.getContent())
          .setControlCode(controlListEntries.getRating());
      if (navigationLevel.getBreadcrumbs() != null) {
        Breadcrumbs breadcrumbs = navigationLevel.getBreadcrumbs();
        controlEntry.setSummaryDescription(breadcrumbs.getBreadcrumbText());
      }
      if (navigationLevel.getNesting() != null) {
        controlEntry.setNested(true);
      } else {
        controlEntry.setNested(false);
      }
      if (navigationLevel.getButtons() != null) {
        controlEntry.setSelectable(true);
      } else {
        controlEntry.setSelectable(false);
      }
      controlEntryId = controlEntryDao.insertControlEntry(controlEntry);

      Logger.debug("Inserted control entry id {}", controlEntryId);
    }

    navigationLevel.getLoadingMetadata().setControlEntryId(controlEntryId);

    for (NavigationLevel subNavigationLevel : navigationLevel.getSubNavigationLevels()) {
      createControlEntries(controlEntryId, subNavigationLevel);
    }
  }

  private void createStages(long journeyId, NavigationLevel navigationLevel) {
    // Make the stage for all sub navigation levels of this navigation level (skip rating-only rows without buttons)
    if (navigationLevel.getSubNavigationLevels().isEmpty() || navigationLevel.getSubNavigationLevels().get(0).getButtons() == null) {
      return;
    }
    NavigationLevel topSubNavigationLevel = navigationLevel.getSubNavigationLevels().get(0);
    Stage stage = new Stage();
    stage.setJourneyId(journeyId);
    stage.setQuestionType(QuestionType.STANDARD);
    stage.setTitle(topSubNavigationLevel.getOnPageContent().getTitle());
    stage.setExplanatoryNotes(topSubNavigationLevel.getOnPageContent().getExplanatoryNotes());
    stage.setAnswerType(mapButtonsToAnswerType(topSubNavigationLevel.getButtons()));
    stage.setControlEntryId(navigationLevel.getLoadingMetadata().getControlEntryId());

    Long stageId = stageDao.insertStage(stage);

    Logger.debug("Inserted stage id {}", stageId);

    for (NavigationLevel subNavigationLevel : navigationLevel.getSubNavigationLevels()) {
      subNavigationLevel.getLoadingMetadata().setStageId(stageId);
      createStages(journeyId, subNavigationLevel);
    }
  }

  private void createStageAnswersAndDecontrolStages(boolean isRoot, long journeyId, int displayOrder, NavigationLevel navigationLevel) {
    if (!isRoot) {
      LoadingMetadata loadingMetadata = navigationLevel.getLoadingMetadata();
      StageAnswer stageAnswer = new StageAnswer();
      stageAnswer.setParentStageId(loadingMetadata.getStageId());

      Decontrols decontrols = navigationLevel.getDecontrols();
      if (decontrols != null && decontrols.getContent() != null) {
        long decontrolStageId = createDecontrolStage(journeyId, navigationLevel);
        stageAnswer.setGoToStageId(decontrolStageId);
      } else {
        if (!navigationLevel.getSubNavigationLevels().isEmpty()) {
          NavigationLevel subNavigationLevel = navigationLevel.getSubNavigationLevels().get(0);
          if (subNavigationLevel.getButtons() == null) {
            //Child entries without buttons are not a stage, we are actually on a leaf now
            stageAnswer.setGoToStageAnswerOutcomeType(StageAnswerOutcomeType.CONTROL_ENTRY_FOUND);
          } else {
            stageAnswer.setGoToStageId(subNavigationLevel.getLoadingMetadata().getStageId());
          }
        } else {
          stageAnswer.setGoToStageAnswerOutcomeType(StageAnswerOutcomeType.CONTROL_ENTRY_FOUND);
        }
      }

      ControlListEntries controlListEntries = navigationLevel.getControlListEntries();
      if (controlListEntries != null) {
        if (controlListEntries.getRating() != null) {
          stageAnswer.setControlEntryId(loadingMetadata.getControlEntryId());
        } else {
          stageAnswer.setAnswerText(navigationLevel.getContent());
        }
        if (controlListEntries.getPriority() != null) {
          stageAnswer.setAnswerPrecedence(controlListEntries.getPriority());
        }
      } else {
        stageAnswer.setAnswerText(navigationLevel.getContent());
      }
      stageAnswer.setDisplayOrder(displayOrder);
      NavigationExtras navigationExtras = navigationLevel.getNavigationExtras();
      if (navigationExtras != null && navigationExtras.isDivLine()) {
        stageAnswer.setDividerAbove(true);
      } else {
        stageAnswer.setDividerAbove(false);
      }

      Long stageAnswerId = stageAnswerDao.insertStageAnswer(stageAnswer);

      Logger.debug("Inserted stage answer id {}", stageAnswerId);

      loadingMetadata.setStageAnswerId(stageAnswerId);
    }

    for (int i = 0; i < navigationLevel.getSubNavigationLevels().size(); i++) {
      NavigationLevel subNavigationLevel = navigationLevel.getSubNavigationLevels().get(i);
      //Don't make StageAnswers for rating-only rows without buttons
      if (subNavigationLevel.getButtons() != null) {
        createStageAnswersAndDecontrolStages(false, journeyId, i + 1, subNavigationLevel);
      }
    }
  }

  private long createDecontrolStage(long journeyId, NavigationLevel navigationLevel) {
    Decontrols decontrols = navigationLevel.getDecontrols();
    LoadingMetadata loadingMetadata = navigationLevel.getLoadingMetadata();

    Stage decontrolStage = new Stage();
    decontrolStage.setJourneyId(journeyId);
    decontrolStage.setAnswerType(AnswerType.SELECT_MANY);
    decontrolStage.setQuestionType(QuestionType.DECONTROL);
    decontrolStage.setControlEntryId(loadingMetadata.getControlEntryId());

    if (!navigationLevel.getSubNavigationLevels().isEmpty()) {
      decontrolStage.setNextStageId(navigationLevel.getSubNavigationLevels().get(0).getLoadingMetadata().getStageId());
    } else {
      decontrolStage.setStageOutcomeType(StageOutcomeType.CONTROL_ENTRY_FOUND);
    }

    Long decontrolStageId = stageDao.insertStage(decontrolStage);

    Logger.debug("Inserted stage id {} (DECONTROL)", decontrolStageId);

    List<String> decontrolEntries = Arrays.stream(decontrols.getContent().split("\\r?\\n"))
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toList());
    for (int i = 0; i < decontrolEntries.size(); i++) {
      StageAnswer decontrolStageAnswer = new StageAnswer();
      decontrolStageAnswer.setParentStageId(decontrolStageId);
      decontrolStageAnswer.setGoToStageAnswerOutcomeType(StageAnswerOutcomeType.DECONTROL);
      decontrolStageAnswer.setAnswerText(decontrolEntries.get(i));
      decontrolStageAnswer.setDisplayOrder(i + 1);
      decontrolStageAnswer.setDividerAbove(false);

      Long decontrolStageAnswerId = stageAnswerDao.insertStageAnswer(decontrolStageAnswer);

      Logger.debug("Inserted stage answer id {} DECONTROL", decontrolStageAnswerId);
    }

    return decontrolStageId;
  }

  private void createLocalDefinitions(NavigationLevel navigationLevel) {
    Definitions definitions = navigationLevel.getDefinitions();

    if (definitions != null && definitions.getLocal() != null) {
      List<String> localDefinitionStrs = Arrays.stream(definitions.getLocal().split("\\r?\\n"))
          .filter(StringUtils::isNotBlank)
          .map(String::trim)
          .collect(Collectors.toList());

      for (String localDefinitionStr : localDefinitionStrs) {
        int firstIdx = localDefinitionStr.indexOf('\'');
        int secondIdx = localDefinitionStr.indexOf('\'', firstIdx + 1);
        String term = localDefinitionStr.substring(firstIdx + 1, secondIdx).toLowerCase();
        if (StringUtils.isBlank(term)) {
          Logger.error("Error deriving term from local definition {}, navigation cell address {}", localDefinitionStr, navigationLevel.getCellAddress());
        } else {
          LocalDefinition localDefinition = new LocalDefinition();
          localDefinition.setControlEntryId(navigationLevel.getLoadingMetadata().getControlEntryId());
          localDefinition.setTerm(term);
          localDefinition.setDefinitionText(localDefinitionStr);

          Long localDefinitionId = localDefinitionDao.insertLocalDefinition(localDefinition);

          Logger.debug("Inserted local definition id {}", localDefinitionId);
        }
      }
    }

    for (NavigationLevel subNavigationLevel : navigationLevel.getSubNavigationLevels()) {
      createLocalDefinitions(subNavigationLevel);
    }
  }

  private void createNotes(NavigationLevel navigationLevel) {
    Notes notes = navigationLevel.getNotes();

    if (notes != null) {
      long nonNullCount = Stream.of(notes.getNb(), notes.getNote(), notes.getSeeAlso(), notes.getTechNote())
          .filter(Objects::nonNull)
          .map(String::trim)
          .filter(StringUtils::isNotBlank)
          .count();

      if (nonNullCount == 1) {
        Note note = new Note();
        note.setStageId(navigationLevel.getLoadingMetadata().getStageId());
        if (notes.getNb() != null) {
          note.setNoteText(notes.getNb());
          note.setNoteType(NoteType.NB);
        } else if (notes.getNote() != null) {
          note.setNoteText(notes.getNote());
          note.setNoteType(NoteType.NOTE);
        } else if (notes.getSeeAlso() != null) {
          note.setNoteText(notes.getSeeAlso());
          note.setNoteType(NoteType.SEE_ALSO);
        } else {
          note.setNoteText(notes.getTechNote());
          note.setNoteType(NoteType.TECH_NOTE);
        }

        Long noteId = noteDao.insertNote(note);

        Logger.debug("Inserted note with id {}", noteId);

      } else if (nonNullCount > 1){
        Logger.error("Error deriving note from note columns, multiple columns populated, navigation cell address {}", navigationLevel.getCellAddress());
      }
    }

    for (NavigationLevel subNavigationLevel : navigationLevel.getSubNavigationLevels()) {
      createNotes(subNavigationLevel);
    }
  }

  private void clearDown() {
    localDefinitionDao.deleteAllLocalDefinitions();
    globalDefinitionDao.deleteAllGlobalDefinitions();
    noteDao.deleteAllNotes();
    stageAnswerDao.deleteAllStageAnswers();
    stageDao.deleteAllStages();
    controlEntryDao.deleteAllControlEntries();
    journeyDao.deleteAllJournies();
  }

  private AnswerType mapButtonsToAnswerType(Buttons buttons) {
    if (buttons == Buttons.SELECT_ONE) {
      return AnswerType.SELECT_ONE;
    } else if (buttons == Buttons.SELECT_MANY) {
      return AnswerType.SELECT_MANY;
    } else {
      return null;
    }
  }

}
