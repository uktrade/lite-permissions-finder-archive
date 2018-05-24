package components.cms.loader;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.GlobalDefinitionDao;
import components.cms.dao.JourneyDao;
import components.cms.dao.LocalDefinitionDao;
import components.cms.dao.NoteDao;
import components.cms.dao.StageAnswerDao;
import components.cms.dao.StageDao;
import components.cms.parser.ParserResult;
import components.cms.parser.model.LoadingMetadata;
import components.cms.parser.model.NavigationLevel;
import components.cms.parser.model.definition.Definition;
import components.cms.parser.model.navigation.column.Breadcrumbs;
import components.cms.parser.model.navigation.column.Buttons;
import components.cms.parser.model.navigation.column.ControlListEntries;
import components.cms.parser.model.navigation.column.Decontrols;
import components.cms.parser.model.navigation.column.Definitions;
import components.cms.parser.model.navigation.column.NavigationExtras;
import components.cms.parser.model.navigation.column.Notes;
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
import models.cms.enums.StageOutcomeType;
import org.apache.commons.lang3.StringUtils;
import play.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

  public void load(ParserResult parserResult) {
    clearDown();
    NavigationLevel rootNavigationLevel = new NavigationLevel("ROOT", "ROOT", -1);
    rootNavigationLevel.addAllSubNavigationlevels(parserResult.getNavigationLevels());
    Journey journey = new Journey().setJourneyName("MILITARY");
    Long journeyId = journeyDao.insertJourney(journey);
    generateLoadingMetadataId(true, rootNavigationLevel, "", 0);
    createControlEntries(null, rootNavigationLevel);
    createStages(journeyId, rootNavigationLevel);
    createStageAnswersAndDecontrolStages(true, journeyId, 1, rootNavigationLevel);
    createLocalDefinitions(rootNavigationLevel);
    createGlobalDefinitions(parserResult.getDefinitions(), journeyId);
    createNotes(rootNavigationLevel);

    // Resolve initial stage id from stage associated with first sub-level of navigation levels
    Long initialStageId = rootNavigationLevel.getSubNavigationLevels().get(0).getLoadingMetadata().getStageId();
    journey = journeyDao.getJourney(journeyId);
    journey.setInitialStageId(initialStageId);
    journeyDao.updateJourney(journeyId, journey);
  }

  public void generateLoadingMetadataId(boolean isRoot, NavigationLevel navigationLevel, String parentId, int index) {
    String id;
    if (parentId.length() == 0) {
      if (isRoot) {
        id = parentId;
      } else {
        id = Integer.toString(index + 1);
      }
    } else {
      id = parentId + "-" + (index + 1);
    }

    LoadingMetadata loadingMetadata = navigationLevel.getLoadingMetadata();
    loadingMetadata.setId(id);

    Logger.debug("Generated id {} for cell {}", id, navigationLevel.getCellAddress());

    ArrayList<NavigationLevel> subNavigationLevels = navigationLevel.getSubNavigationLevels();

    for (int i = 0; i < subNavigationLevels.size(); i++) {
      NavigationLevel subNavigationLevel = subNavigationLevels.get(i);
      generateLoadingMetadataId(false, subNavigationLevel, id, i);
    }
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
    decontrolStage.setTitle(decontrols.getTitle());
    decontrolStage.setExplanatoryNotes(decontrols.getExplanatoryNotes());

    if (!navigationLevel.getSubNavigationLevels().isEmpty()) {
      decontrolStage.setNextStageId(navigationLevel.getSubNavigationLevels().get(0).getLoadingMetadata().getStageId());
    } else {
      decontrolStage.setStageOutcomeType(StageOutcomeType.CONTROL_ENTRY_FOUND);
    }

    Long decontrolStageId = stageDao.insertStage(decontrolStage);

    Logger.debug("Inserted stage id {} (DECONTROL)", decontrolStageId);

    List<String> decontrolEntries =
        Arrays.stream(decontrols.getContent().split("\\r?\\n(?!\\*)"))
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());

    for (int i = 0; i < decontrolEntries.size(); i++) {
      StageAnswer decontrolStageAnswer = new StageAnswer();

      List<String> tokens =
          Arrays.stream(decontrolEntries.get(i).split("\\r?\\n"))
              .filter(StringUtils::isNotBlank)
              .collect(Collectors.toList());

      if (!tokens.isEmpty()) {
        decontrolStageAnswer.setAnswerText(tokens.get(0));
        if (tokens.size() > 1) {
          String nestedContent = tokens.subList(1, tokens.size()).stream().collect(Collectors.joining("\n"));
          decontrolStageAnswer.setNestedContent(nestedContent);
        }
      } else {
        Logger.error("Unable to derive answer text for decontrol stage answer, stage id {}, cell id {}", decontrolStageId, navigationLevel.getCellAddress());
      }

      decontrolStageAnswer.setParentStageId(decontrolStageId);
      decontrolStageAnswer.setGoToStageAnswerOutcomeType(StageAnswerOutcomeType.DECONTROL);
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
      if (notes.getNb() != null) {
        splitAndInsertNote(notes.getNb(), NoteType.NB, navigationLevel);
      } else if (notes.getNote() != null) {
        splitAndInsertNote(notes.getNote(), NoteType.NOTE, navigationLevel);
      } else if (notes.getSeeAlso() != null) {
        splitAndInsertNote(notes.getSeeAlso(), NoteType.SEE_ALSO, navigationLevel);
      } else if (notes.getTechNote() != null) {
        splitAndInsertNote(notes.getTechNote(), NoteType.TECH_NOTE, navigationLevel);
      }
    }

    for (NavigationLevel subNavigationLevel : navigationLevel.getSubNavigationLevels()) {
      createNotes(subNavigationLevel);
    }
  }

  private void splitAndInsertNote(String noteText, NoteType noteType, NavigationLevel navigationLevel) {
    long stageAnswerId = navigationLevel.getLoadingMetadata().getStageAnswerId();
    StageAnswer stageAnswer = stageAnswerDao.getStageAnswer(stageAnswerId);
    Long stageId = stageAnswer.getGoToStageId();

    if (stageId == null) {
      Logger.error("No stage id to associate note with, stage answer id {}, cell id {}", stageAnswerId, navigationLevel.getCellAddress());
      return;
    }

    List<String> noteTexts = Arrays.stream(noteText.split("\\r?\\n"))
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toList());

    for (String nt : noteTexts) {
      Note note = new Note()
          .setStageId(stageId)
          .setNoteType(noteType)
          .setNoteText(nt);

      Long noteId = noteDao.insertNote(note);

      Logger.debug("Inserted note with id {}", noteId);
    }
  }

  private void createGlobalDefinitions(List<Definition> definitions, long journeyId) {
    for (Definition definition : definitions) {
      if ("UK Military List".equalsIgnoreCase(definition.getList())) {
        String term = StringUtils.strip(StringUtils.trimToEmpty(definition.getName()), "\"").toLowerCase();
        String definitionText = definition.getNewContent();
        if (StringUtils.isAnyEmpty(term, definitionText)) {
          Logger.error("Invalid global definition, row num {}, term {}, definition text {}", definition.getRowNum(), term,
              definitionText);
        } else {
          GlobalDefinition globalDefinition = new GlobalDefinition()
              .setJourneyId(journeyId)
              .setTerm(term)
              .setDefinitionText(definitionText);

          Long globalDefinitionId = globalDefinitionDao.insertGlobalDefinition(globalDefinition);

          Logger.debug("Inserted global definition with id {}", globalDefinitionId);
        }
      }
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
