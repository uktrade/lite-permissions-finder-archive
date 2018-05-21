package triage.config;

import com.google.inject.Inject;
import triage.text.RichText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class JourneyConfigServiceSampleImpl implements JourneyConfigService {

  public static final String STAGE_1_ID = "1";
  public static final String STAGE_2_ID = "2";
  public static final String STAGE_3_ID = "3";
  public static final String STAGE_4_ID = "4";
  public static final String STAGE_5_ID = "5";
  private final Map<String, StageConfig> configMap = new HashMap<>();

  @Inject
  public JourneyConfigServiceSampleImpl() {

    ControlEntryConfig ml1 = new ControlEntryConfig("1", "ML1",
        new RichText("Smooth-bore weapons with a calibre of less than 20mm"), new RichText("Smooth-bore weapons"), null,
        true, false);

    ControlEntryConfig ml1a = new ControlEntryConfig("2", "ML1a",
        new RichText("Rifles and combination guns, handguns and machine guns"),
        new RichText("Rifles"), ml1, true, false);

    ControlEntryConfig ml1b = new ControlEntryConfig("3", "ML1b", new RichText("Smooth-bore weapons"), null, ml1, false,
        false);

    ControlEntryConfig ml1c = new ControlEntryConfig("4", "ML1c", new RichText("Weapons using caseless ammunition"), null,
        ml1, false, false);

    List<AnswerConfig> stage1Answers = new ArrayList<>();
    stage1Answers.add(new AnswerConfig("A1", "2", null, ml1a, 1, false));
    stage1Answers.add(new AnswerConfig("A2", "3", null, ml1b, 2, false));
    stage1Answers.add(new AnswerConfig("A3", "4", null, ml1c, 3, false));

    List<AnswerConfig> outcomeAnswers = new ArrayList<>();
    outcomeAnswers.add(new AnswerConfig("B1", null, AnswerConfig.OutcomeType.CONTROL_ENTRY_FOUND, ml1a, 1, false));
    outcomeAnswers.add(new AnswerConfig("B2", null, AnswerConfig.OutcomeType.CONTROL_ENTRY_FOUND, ml1b, 2, false));
    outcomeAnswers.add(new AnswerConfig("B3", null, AnswerConfig.OutcomeType.CONTROL_ENTRY_FOUND, ml1c, 3, false));

    StageConfig stage1 = new StageConfig(STAGE_1_ID, "Question for the first stage", new RichText("This is an explanatory note."),
        StageConfig.QuestionType.STANDARD, StageConfig.AnswerType.SELECT_ONE, null, ml1, stage1Answers);

    configMap.put(STAGE_1_ID, stage1);

    StageConfig stage2 = new StageConfig(STAGE_2_ID, null, null, StageConfig.QuestionType.DECONTROL,
        StageConfig.AnswerType.SELECT_MANY, STAGE_3_ID, ml1a, stage1Answers);

    configMap.put(STAGE_2_ID, stage2);

    StageConfig stage3 = new StageConfig(STAGE_3_ID, null, null, StageConfig.QuestionType.STANDARD,
        StageConfig.AnswerType.SELECT_ONE, null, ml1b, outcomeAnswers);

    configMap.put(STAGE_3_ID, stage3);

    StageConfig stage4 = new StageConfig(STAGE_4_ID, null, new RichText("This is another explanatory note."), StageConfig.QuestionType.STANDARD,
        StageConfig.AnswerType.SELECT_MANY, null, ml1c, outcomeAnswers);

    configMap.put(STAGE_4_ID, stage4);
  }

  @Override
  public String getInitialStageId() {
    return STAGE_1_ID;
  }

  @Override
  public StageConfig getStageConfigById(String stageId) {
    return configMap.get(stageId);
  }

  @Override
  public List<NoteConfig> getNoteConfigsByStageId(String stageId) {
    NoteConfig noteConfigOne = new NoteConfig(
        UUID.randomUUID().toString(), stageId, new RichText("This is an example note"), NoteConfig.NoteType.NOTE);
    NoteConfig noteConfigTwo = new NoteConfig(
        UUID.randomUUID().toString(), stageId, new RichText("This is another example note"), NoteConfig.NoteType.NOTE);
    return Arrays.asList(noteConfigOne, noteConfigTwo);
  }

  @Override
  public ControlEntryConfig getControlEntryConfigById(String controlEntryId) {
    return null;
  }

  @Override
  public List<ControlEntryConfig> getChildRatings(ControlEntryConfig controlEntryConfig) {
    if (!controlEntryConfig.hasNestedChildren() || controlEntryConfig.getControlCode().length() > 20) {
      return new ArrayList<>();
    } else {
      String controlCode = controlEntryConfig.getControlCode();
      return Arrays.asList(createControlEntryConfig(controlCode + "Child1"),
          createControlEntryConfig(controlCode + "Child2"));
    }
  }

  @Override
  public List<String> getStageIdsForControlEntry(ControlEntryConfig controlEntryConfig) {
    String controlCode = controlEntryConfig.getControlCode();
    return configMap.entrySet().stream()
        .filter(entry -> {
          Optional<ControlEntryConfig> controlEntryConfigOptional = entry.getValue().getRelatedControlEntry();
          if (controlEntryConfigOptional.isPresent()) {
            return controlCode.equals(controlEntryConfigOptional.get().getControlCode());
          } else {
            return false;
          }
        })
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  private ControlEntryConfig createControlEntryConfig(String controlCode) {
    return new ControlEntryConfig(controlCode + "_ID", controlCode,
        new RichText(controlCode + "FullDescription"),
        new RichText(controlCode + "SummaryDescription"),
        null,
        true,
        false);
  }

}
