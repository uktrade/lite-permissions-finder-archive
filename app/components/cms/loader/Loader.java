package components.cms.loader;

import com.google.inject.Inject;
import components.cms.dao.*;
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
import lombok.AllArgsConstructor;
import models.cms.*;
import models.cms.enums.AnswerType;
import models.cms.enums.NoteType;
import models.cms.enums.OutcomeType;
import models.cms.enums.QuestionType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
      Journey journey = new Journey().setJourneyName(rootNavigationLevel.getList());
      Long journeyId = journeyDao.insertJourney(journey);

      // Populate the database
      createGlobalDefinitions(parserResult.getDefinitions(), journeyId,
          rootNavigationLevel.getList());
      generateLoadingMetadataId(true, rootNavigationLevel, "", 0);
      createControlEntries(null, 1, rootNavigationLevel, journeyId);
      createStages(journeyId, rootNavigationLevel);
      createStageAnswersAndDecontrolStages(true, journeyId, 1, rootNavigationLevel);
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
    spreadsheetVersionDao.insert(spreadsheetVersion.getFilename(), spreadsheetVersion.getVersion(), spreadsheetVersion.getSha1());
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


  private void createControlEntries(Long parentControlEntryId, int displayOrder,
      NavigationLevel navigationLevel,
      long journeyId) {
    Long controlEntryId = null;

    ControlListEntries controlListEntries = navigationLevel.getControlListEntries();
    if (controlListEntries != null && controlListEntries.getRating() != null) {
      ControlEntry controlEntry = new ControlEntry()
          .setParentControlEntryId(parentControlEntryId)
          .setFullDescription(navigationLevel.getContent())
          .setControlCode(controlListEntries.getRating())
          .setDecontrolled(controlListEntries.isDecontrolled())
          .setJumpToControlCodes(navigationLevel.getLoops().getJumpToControlCodes());
      if (navigationLevel.getBreadcrumbs() != null) {
        Breadcrumbs breadcrumbs = navigationLevel.getBreadcrumbs();
        controlEntry.setSummaryDescription(breadcrumbs.getBreadcrumbText());
      }
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
      decontrolled = topSubNavigationLevel.getControlListEntries().isDecontrolled();
    }

    Stage stage = new Stage()
        .setJourneyId(journeyId)
        .setQuestionType(QuestionType.STANDARD)
        .setTitle(topSubNavigationLevel.getOnPageContent().getTitle())
        .setExplanatoryNotes(topSubNavigationLevel.getOnPageContent().getExplanatoryNotes())
        .setAnswerType(mapButtonsToAnswerType(topSubNavigationLevel.getButtons()))
        .setDecontrolled(decontrolled)
        .setControlEntryId(navigationLevel.getLoadingMetadata().getControlEntryId());

    Long stageId = stageDao.insertStage(stage);

    LOGGER.debug("Inserted stage id {}", stageId);

    // Create stages for sub navigation levels
    for (NavigationLevel subNavigationLevel : navigationLevel.getSubNavigationLevels()) {
      subNavigationLevel.getLoadingMetadata().setStageId(stageId);
      createStages(journeyId, subNavigationLevel);
    }
  }

  private void createStageAnswersAndDecontrolStages(boolean isRoot, long journeyId,
      int displayOrder,
      NavigationLevel navigationLevel) {
    if (!isRoot) {
      StageAnswer stageAnswer = new StageAnswer();
      stageAnswer.setStageId(navigationLevel.getLoadingMetadata().getStageId());

      Long attachNotesToStageId = null;

      Decontrols decontrols = navigationLevel.getDecontrols();
      if (decontrols != null && decontrols.getContent() != null) {
        Stage decontrolStage = createDecontrolStage(journeyId, navigationLevel);
        stageAnswer.setGoToStageId(decontrolStage.getId());
        attachNotesToStageId = decontrolStage.getNextStageId();
      } else {
        if (!navigationLevel.getSubNavigationLevels().isEmpty()) {
          NavigationLevel subNavigationLevel = navigationLevel.getSubNavigationLevels().get(0);
          if (subNavigationLevel.getButtons() == null) {
            //Child entries without buttons are not a stage, we are actually on a leaf now
            if (navigationLevel.getRedirect().isTooComplexForCodeFinder()) {
              stageAnswer.setGoToOutcomeType(OutcomeType.TOO_COMPLEX);
            } else {
              stageAnswer.setGoToStageId(createItemStage(journeyId,
                  navigationLevel.getLoadingMetadata().getControlEntryId(),
                  navigationLevel.getNotes()));
            }
          } else {
            Long goToStageId = subNavigationLevel.getLoadingMetadata().getStageId();
            stageAnswer.setGoToStageId(goToStageId);
            attachNotesToStageId = goToStageId;
          }
        } else {
          //Child entries without buttons are not a stage, we are actually on a leaf now
          if (navigationLevel.getRedirect().isTooComplexForCodeFinder()) {
            stageAnswer.setGoToOutcomeType(OutcomeType.TOO_COMPLEX);
          } else {
            stageAnswer.setGoToStageId(
                createItemStage(journeyId, navigationLevel.getLoadingMetadata().getControlEntryId(),
                    navigationLevel.getNotes()));
          }
        }
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
      NavigationExtras navigationExtras = navigationLevel.getNavigationExtras();
      stageAnswer.setDividerAbove(navigationExtras != null && navigationExtras.isDivLine());

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
        createStageAnswersAndDecontrolStages(false, journeyId, i + 1, subNavigationLevel);
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

  private Stage createDecontrolStage(long journeyId, NavigationLevel navigationLevel) {
    Decontrols decontrols = navigationLevel.getDecontrols();
    LoadingMetadata loadingMetadata = navigationLevel.getLoadingMetadata();

    Stage decontrolStage = new Stage()
        .setJourneyId(journeyId)
        .setAnswerType(AnswerType.SELECT_MANY)
        .setQuestionType(QuestionType.DECONTROL)
        .setControlEntryId(loadingMetadata.getControlEntryId())
        .setTitle(decontrols.getTitle())
        .setExplanatoryNotes(decontrols.getExplanatoryNotes());

    if (!navigationLevel.getSubNavigationLevels().isEmpty()) {
      Long nextStageId = navigationLevel.getSubNavigationLevels().get(0).getLoadingMetadata()
          .getStageId();
      // This can happen if the current row has "nested content" child rows which are not actual stages
      if (nextStageId == null) {
        LOGGER.error("Next stage ID null for decontrol stage {}, assuming outcome",
            navigationLevel.getCellAddress());
        if (navigationLevel.getRedirect().isTooComplexForCodeFinder()) {
          decontrolStage.setStageOutcomeType(OutcomeType.TOO_COMPLEX);
        } else {
          decontrolStage.setNextStageId(
              createItemStage(journeyId, loadingMetadata.getControlEntryId(),
                  navigationLevel.getNotes()));
        }
      } else {
        decontrolStage.setNextStageId(nextStageId);
      }
    } else {
      if (navigationLevel.getRedirect().isTooComplexForCodeFinder()) {
        decontrolStage.setStageOutcomeType(OutcomeType.TOO_COMPLEX);
      } else {
        decontrolStage.setNextStageId(
            createItemStage(journeyId, loadingMetadata.getControlEntryId(),
                navigationLevel.getNotes()));
      }
    }

    Long decontrolStageId = stageDao.insertStage(decontrolStage);
    decontrolStage.setId(decontrolStageId);

    LOGGER.debug("Inserted stage id {} (DECONTROL)", decontrolStageId);

    List<String> decontrolEntries =
        Arrays.stream(decontrols.getContent().split("\\r?\\n(?!\\*)"))
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());

    for (int i = 0; i < decontrolEntries.size(); i++) {
      StageAnswer decontrolStageAnswer = new StageAnswer();

      List<String> tokens =
          Arrays.stream(decontrolEntries.get(i).split(REGEX_NEW_LINE))
              .filter(StringUtils::isNotBlank)
              .collect(Collectors.toList());

      if (!tokens.isEmpty()) {
        decontrolStageAnswer.setAnswerText(tokens.get(0));
        if (tokens.size() > 1) {
          String nestedContent = tokens.subList(1, tokens.size()).stream()
              .collect(Collectors.joining("\n", "", "\n"));
          decontrolStageAnswer.setNestedContent(nestedContent);
        }
      } else {
        LOGGER.error(
            "Unable to derive answer text for decontrol stage answer, stage id {}, cell id {}",
            decontrolStageId, navigationLevel.getCellAddress());
      }

      decontrolStageAnswer.setStageId(decontrolStageId);
      decontrolStageAnswer.setGoToOutcomeType(OutcomeType.DECONTROL);
      decontrolStageAnswer.setDisplayOrder(i + 1);
      decontrolStageAnswer.setDividerAbove(false);

      Long decontrolStageAnswerId = stageAnswerDao.insertStageAnswer(decontrolStageAnswer);

      LOGGER.debug("Inserted stage answer id {} DECONTROL", decontrolStageAnswerId);
    }

    return decontrolStage;
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
    List<String> noteTexts = Arrays.stream(noteText.split(REGEX_NEW_LINE))
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toList());

    for (String nt : noteTexts) {
      Note note = new Note()
          .setStageId(stageId)
          .setNoteType(noteType)
          .setNoteText(nt);

      Long noteId = noteDao.insertNote(note);

      LOGGER.debug("Inserted note with id {}", noteId);
    }
  }

  private void createGlobalDefinitions(List<Definition> definitions, long journeyId,
      String sheetName) {
    for (Definition definition : definitions) {
      if (sheetName.equalsIgnoreCase(definition.getList())) {
        String term = StringUtils.strip(StringUtils.trimToEmpty(definition.getName()), "\"");
        String definitionText = definition.getNewContent();

        if (StringUtils.isAnyEmpty(term, definitionText)) {
          LOGGER.error("Invalid global definition, row num {}, term {}, definition text {}",
              definition.getRowNumber(), term,
              definitionText);
        } else {
          GlobalDefinition globalDefinition = new GlobalDefinition(journeyId, term, definitionText);

          Long globalDefinitionId = globalDefinitionDao.insertGlobalDefinition(globalDefinition);

          LOGGER.debug("Inserted global definition with id {}", globalDefinitionId);
        }
      }
    }
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
            LOGGER
                .error("No control entry record found for related code {} in list {}", relatedCode,
                    navigationLevel.getList());
          } else {
            RelatedControlEntry relatedControlEntry = new RelatedControlEntry()
                .setControlEntryId(loadingMetadata.getControlEntryId())
                .setRelatedControlEntryId(controlEntry.getId());
            relatedControlEntryDao.insertRelatedControlEntry(relatedControlEntry);

            LOGGER.debug(
                "Inserted related control entry: control entry id {}, related control entry id {}",
                relatedControlEntry.getControlEntryId(),
                relatedControlEntry.getRelatedControlEntryId());
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
    journeyDao.deleteAllJournies();
  }

  private AnswerType mapButtonsToAnswerType(Buttons buttons) {
    switch (buttons) {
      case SELECT_ONE:
        return AnswerType.SELECT_ONE;
      case SELECT_MANY:
        return AnswerType.SELECT_MANY;
      default:
        return null;
    }
  }
}
