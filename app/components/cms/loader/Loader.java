package components.cms.loader;

import static components.cms.parser.model.navigation.column.Buttons.SELECT_MANY;
import static components.cms.parser.model.navigation.column.Buttons.SELECT_ONE;

import com.google.inject.Inject;
import components.cms.dao.ControlEntryDao;
import components.cms.dao.GlobalDefinitionDao;
import components.cms.dao.JourneyDao;
import components.cms.dao.LocalDefinitionDao;
import components.cms.dao.NoteDao;
import components.cms.dao.RelatedControlEntryDao;
import components.cms.dao.SessionStageDao;
import components.cms.dao.SpreadsheetVersionDao;
import components.cms.dao.StageAnswerDao;
import components.cms.dao.StageDao;
import components.cms.parser.ParserResult;
import components.cms.parser.model.LoadingMetadata;
import components.cms.parser.model.NavigationLevel;
import components.cms.parser.model.definition.Definition;
import components.cms.parser.model.navigation.column.Buttons;
import components.cms.parser.model.navigation.column.ControlListEntries;
import components.cms.parser.model.navigation.column.Definitions;
import components.cms.parser.model.navigation.column.Notes;
import components.cms.parser.model.navigation.column.Redirect;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import models.cms.ControlEntry;
import models.cms.GlobalDefinition;
import models.cms.Journey;
import models.cms.LocalDefinition;
import models.cms.Note;
import models.cms.RelatedControlEntry;
import models.cms.SpreadsheetVersion;
import models.cms.Stage;
import models.cms.StageAnswer;
import models.cms.enums.AnswerType;
import models.cms.enums.NoteType;
import models.cms.enums.OutcomeType;
import models.cms.enums.QuestionType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class Loader {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Loader.class);

  private static final String REGEX_NEW_LINE = "\\r?\\n";

  private final ControlEntryDao controlEntryDao;
  private final GlobalDefinitionDao globalDefinitionDao;
  private final JourneyDao journeyDao;
  private final LocalDefinitionDao localDefinitionDao;
  private final NoteDao noteDao;
  private final StageAnswerDao stageAnswerDao;
  private final StageDao stageDao;
  private final SessionStageDao sessionStageDao;
  private final RelatedControlEntryDao relatedControlEntryDao;
  private final SpreadsheetVersionDao spreadsheetVersionDao;

  /**
   * Populates the database
   *
   * @param parserResult contains definitions and navigation levels
   */
  public void load(ParserResult parserResult) {
    // Empty the database before insertion
    clearDatabase();

    // Loop through available sheets (eg Military, Dual Use)
    for (NavigationLevel rootNavigationLevel : parserResult.getNavigationLevels()) {
      // Generate a new journey for each sheet
      Journey journey = new Journey().setJourneyName(rootNavigationLevel.getList()).setFriendlyJourneyName(rootNavigationLevel.getFriendlyName());
      Long journeyId = journeyDao.insertJourney(journey);

      // Populate the database
      createGlobalDefinitions(parserResult.getDefinitions(), journeyId, rootNavigationLevel.getList());
      generateLoadingMetadataId(true, rootNavigationLevel, "", 0);
      createControlEntries(null, 1, rootNavigationLevel, journeyId);
      createStages(journeyId, rootNavigationLevel);
      createStageAnswers(true, journeyId, 1, rootNavigationLevel);
      createLocalDefinitions(rootNavigationLevel);
      createRelatedControlEntries(true, rootNavigationLevel);

      // Resolve initial stage id from stage associated with first sub-level of navigation levels
      Long initialStageId = rootNavigationLevel.getSubNavigationLevels().get(0).getLoadingMetadata()
          .getStageId();
      journey = journeyDao.getJourney(journeyId);
      journey.setInitialStageId(initialStageId);
      journeyDao.updateJourney(journeyId, journey);
    }

    // Insert version database
    SpreadsheetVersion spreadsheetVersion = parserResult.getSpreadsheetVersion();
    spreadsheetVersionDao.insert(spreadsheetVersion.getFilename(), spreadsheetVersion.getVersion(),
      spreadsheetVersion.getSha1());
  }

  private void generateLoadingMetadataId(boolean isRoot, NavigationLevel navigationLevel,
      String parentId, int index) {
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

    LOGGER.debug("Generated id {} for cell {}", id, navigationLevel.getCellAddress());

    List<NavigationLevel> subNavigationLevels = navigationLevel.getSubNavigationLevels();

    for (int i = 0; i < subNavigationLevels.size(); i++) {
      NavigationLevel subNavigationLevel = subNavigationLevels.get(i);
      generateLoadingMetadataId(false, subNavigationLevel, id, i);
    }
  }


  private void createControlEntries(Long parentControlEntryId, int displayOrder, NavigationLevel navigationLevel,
    long journeyId) {
    Long controlEntryId = null;

    ControlListEntries controlListEntries = navigationLevel.getControlListEntries();
    if (controlListEntries != null && controlListEntries.getRating() != null) {
      ControlEntry controlEntry = new ControlEntry()
          .setParentControlEntryId(parentControlEntryId)
          .setDescription(navigationLevel.getContent())
          .setControlCode(controlListEntries.getRating())
          .setDecontrolled(controlListEntries.isDecontrolled())
          .setJumpToControlCodes(navigationLevel.getLoops().getJumpToControlCodes());
      controlEntry.setNested(navigationLevel.getNesting() != null);
      controlEntry.setDisplayOrder(displayOrder);
      controlEntry.setJourneyId(journeyId);
      controlEntryId = controlEntryDao.insertControlEntry(controlEntry);

      LOGGER.debug("Inserted control entry id {}", controlEntryId);
    }

    navigationLevel.getLoadingMetadata().setControlEntryId(controlEntryId);

    for (int i = 0; i < navigationLevel.getSubNavigationLevels().size(); i++) {
      createControlEntries(controlEntryId, i + 1, navigationLevel.getSubNavigationLevels().get(i),
          journeyId);
    }
  }

  private void createStages(long journeyId, NavigationLevel navigationLevel) {
    // Make the stage for all sub navigation levels of this navigation level (skip rating-only rows without buttons)
    if (navigationLevel.getSubNavigationLevels().isEmpty()
        || navigationLevel.getSubNavigationLevels().get(0).getButtons() == null) {
      return;
    }

    // Create stage and push it to database
    NavigationLevel topSubNavigationLevel = navigationLevel.getSubNavigationLevels().get(0);

    boolean decontrolled = false;
    if (topSubNavigationLevel.getControlListEntries() != null) {
      decontrolled = topSubNavigationLevel.getControlListEntries().isDecontrolled() == Boolean.TRUE;
    }

    Stage stage = new Stage()
        .setJourneyId(journeyId)
        .setQuestionType(QuestionType.STANDARD)
        .setTitle(topSubNavigationLevel.getOnPageContent().getTitle())
        .setExplanatoryNotes(topSubNavigationLevel.getOnPageContent().getExplanatoryNotes())
        .setAnswerType(mapButtonsToAnswerType(topSubNavigationLevel.getButtons()))
        .setControlEntryId(navigationLevel.getLoadingMetadata().getControlEntryId());

    Long stageId = stageDao.insertStage(stage);

    LOGGER.debug("Inserted stage id {}", stageId);

    // Create stages for sub navigation levels
    for (NavigationLevel subNavigationLevel : navigationLevel.getSubNavigationLevels()) {
      subNavigationLevel.getLoadingMetadata().setStageId(stageId);
      createStages(journeyId, subNavigationLevel);
    }
  }

  private void createStageAnswers(boolean isRoot, long journeyId, int displayOrder,
    NavigationLevel navigationLevel) {
    if (!isRoot) {
      StageAnswer stageAnswer = new StageAnswer();
      stageAnswer.setStageId(navigationLevel.getLoadingMetadata().getStageId());

      Long attachNotesToStageId = null;

      if (navigationLevel.getSubNavigationLevels().isEmpty() || navigationLevel.getSubNavigationLevels().get(0).getButtons() == null) {
        //Child entries without buttons are not a stage, we are actually on a leaf now
        if (navigationLevel.getRedirect() == Redirect.TOO_COMPLEX_FOR_CODE_FINDER) {
          stageAnswer.setGoToOutcomeType(OutcomeType.TOO_COMPLEX);
        } else {
          stageAnswer.setGoToStageId(createItemStage(
            journeyId,
            navigationLevel.getLoadingMetadata().getControlEntryId(),
            navigationLevel.getNotes()
          ));
        }
      } else {
        Long goToStageId = navigationLevel.getSubNavigationLevels().get(0).getLoadingMetadata().getStageId();
        stageAnswer.setGoToStageId(goToStageId);
        attachNotesToStageId = goToStageId;
      }

      ControlListEntries controlListEntries = navigationLevel.getControlListEntries();
      if (controlListEntries != null) {
        if (controlListEntries.getRating() != null) {
          stageAnswer.setControlEntryId(navigationLevel.getLoadingMetadata().getControlEntryId());
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

      Long stageAnswerId = stageAnswerDao.insertStageAnswer(stageAnswer);

      LOGGER.debug("Inserted stage answer id {}", stageAnswerId);

      if (attachNotesToStageId != null) {
        createNotes(navigationLevel.getNotes(), attachNotesToStageId);
      } else {
        LOGGER.debug("No stageId to associate note with, cell id {}",
            navigationLevel.getCellAddress());
      }
      navigationLevel.getLoadingMetadata().setStageAnswerId(stageAnswerId);
    }

    for (int i = 0; i < navigationLevel.getSubNavigationLevels().size(); i++) {
      NavigationLevel subNavigationLevel = navigationLevel.getSubNavigationLevels().get(i);
      //Don't make StageAnswers for rating-only rows without buttons
      if (subNavigationLevel.getButtons() != null) {
        createStageAnswers(false, journeyId, i + 1, subNavigationLevel);
      }
    }
  }

  private long createItemStage(long journeyId, long controlEntryId, Notes notes) {
    Stage stage = new Stage()
        .setJourneyId(journeyId)
        .setQuestionType(QuestionType.ITEM)
        .setAnswerType(AnswerType.SELECT_ONE)
        .setControlEntryId(controlEntryId);

    Long stageId = stageDao.insertStage(stage);

    LOGGER.debug("Inserted item stage id {}", stageId);

    createNotes(notes, stageId);

    return stageId;
  }

  private void createLocalDefinitions(NavigationLevel navigationLevel) {
    Definitions definitions = navigationLevel.getDefinitions();

    if (definitions != null && definitions.getLocal() != null) {
      List<String> localDefinitionStrs = Arrays.stream(definitions.getLocal().split(REGEX_NEW_LINE))
          .filter(StringUtils::isNotBlank)
          .map(String::trim)
          .collect(Collectors.toList());

      for (String localDefinitionStr : localDefinitionStrs) {
        int firstIdx = localDefinitionStr.indexOf('\'');
        int secondIdx = localDefinitionStr.indexOf('\'', firstIdx + 1);
        String term = localDefinitionStr.substring(firstIdx + 1, secondIdx);
        if (StringUtils.isBlank(term)) {
          LOGGER.error("Error deriving term from local definition {}, navigation cell address {}",
              localDefinitionStr, navigationLevel.getCellAddress());
        } else {
          LocalDefinition localDefinition = new LocalDefinition();
          localDefinition
              .setControlEntryId(navigationLevel.getLoadingMetadata().getControlEntryId());
          localDefinition.setTerm(term);
          localDefinition.setDefinitionText(localDefinitionStr);

          Long localDefinitionId = localDefinitionDao.insertLocalDefinition(localDefinition);

          LOGGER.debug("Inserted local definition id {}", localDefinitionId);
        }
      }
    }

    for (NavigationLevel subNavigationLevel : navigationLevel.getSubNavigationLevels()) {
      createLocalDefinitions(subNavigationLevel);
    }
  }

  private void createNotes(Notes notes, long stageId) {
    if (notes != null && !notes.isCreated()) {
      if (notes.getNb() != null) {
        splitAndInsertNote(notes.getNb(), NoteType.NB, stageId);
      }
      if (notes.getNote() != null) {
        splitAndInsertNote(notes.getNote(), NoteType.NOTE, stageId);
      }
      if (notes.getSeeAlso() != null) {
        splitAndInsertNote(notes.getSeeAlso(), NoteType.SEE_ALSO, stageId);
      }
      if (notes.getTechNote() != null) {
        splitAndInsertNote(notes.getTechNote(), NoteType.TECHNICAL_NOTE, stageId);
      }
      notes.setCreated(true);
    }
  }

  private void splitAndInsertNote(String noteText, NoteType noteType, long stageId) {
    List<Note> notes = Arrays.stream(noteText.split(REGEX_NEW_LINE))
        .filter(StringUtils::isNotBlank)
        .map(note -> new Note(stageId, note.trim(), noteType))
        .collect(Collectors.toList());

    noteDao.insert(notes);

    LOGGER.debug("Successfully inserted notes");
  }

  private void createGlobalDefinitions(List<Definition> definitions, long journeyId, String sheetName) {
    List<GlobalDefinition> globalDefinitions = definitions.parallelStream()
      .filter(globalDefinition -> globalDefinition.getList().equalsIgnoreCase(sheetName))
      .peek(globalDefinition -> globalDefinition.setName(StringUtils.strip(StringUtils.trimToEmpty(globalDefinition.getName()), "\"")))
      .map(globalDefinition -> new GlobalDefinition(journeyId, globalDefinition.getName(), globalDefinition.getNewContent()))
      .collect(Collectors.toList());

    globalDefinitionDao.insert(globalDefinitions);

    LOGGER.debug("Successfully inserted global definitions");
  }

  private void createRelatedControlEntries(boolean isRoot, NavigationLevel navigationLevel) {
    if (!isRoot) {
      String relatedCodes = navigationLevel.getLoops().getRelatedCodes();
      LoadingMetadata loadingMetadata = navigationLevel.getLoadingMetadata();
      if (loadingMetadata.getControlEntryId() != null && StringUtils.isNotBlank(relatedCodes)) {
        List<String> relatedCodeList = Arrays.stream(relatedCodes.split(REGEX_NEW_LINE))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());

        for (String relatedCode : relatedCodeList) {
          ControlEntry controlEntry = controlEntryDao.getControlEntryByControlCode(relatedCode);

          if (controlEntry == null) {
            LOGGER.error("No control entry record found for related code {} in list {}", relatedCode,
              navigationLevel.getList());
          } else {
            RelatedControlEntry relatedControlEntry = new RelatedControlEntry()
                .setControlEntryId(loadingMetadata.getControlEntryId())
                .setRelatedControlEntryId(controlEntry.getId());
            relatedControlEntryDao.insertRelatedControlEntry(relatedControlEntry);

            LOGGER.debug("Inserted related control entry: control entry id {}, related control entry id {}",
              relatedControlEntry.getControlEntryId(), relatedControlEntry.getRelatedControlEntryId());
          }
        }
      }
    }

    for (NavigationLevel subNavigationLevel : navigationLevel.getSubNavigationLevels()) {
      createRelatedControlEntries(false, subNavigationLevel);
    }
  }

  private void clearDatabase() {
    relatedControlEntryDao.deleteAllRelatedControlEntries();
    sessionStageDao.deleteAllSessionStages();
    localDefinitionDao.deleteAllLocalDefinitions();
    globalDefinitionDao.deleteAllGlobalDefinitions();
    noteDao.deleteAllNotes();
    stageAnswerDao.deleteAllStageAnswers();
    stageDao.deleteAllStages();
    controlEntryDao.deleteAllControlEntries();
    journeyDao.deleteAllJourneys();
  }

  private AnswerType mapButtonsToAnswerType(Buttons buttons) {
    return buttons == SELECT_ONE ? AnswerType.SELECT_ONE
      : buttons == SELECT_MANY ? AnswerType.SELECT_MANY
      : null;
  }
}
