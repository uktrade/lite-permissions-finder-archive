package triage.config;

import com.google.inject.Inject;
import triage.text.RichText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JourneyConfigServiceSampleImpl implements JourneyConfigService {

  public static final String STAGE_1_ID = "1";
  public static final String STAGE_2_ID = "2";
  public static final String STAGE_3_ID = "3";
  public static final String STAGE_4_ID = "4";
  public static final String STAGE_5_ID = "5";
  private final Map<String, StageConfig> configMap = new HashMap<>();

  @Inject
  public JourneyConfigServiceSampleImpl() {

    ControlEntryConfig ml1 = new ControlEntryConfig("ML1",
        new RichText("Smooth-bore weapons with a calibre of less than 20mm"), new RichText("Smooth-bore weapons"), null,
        false, false);

    ControlEntryConfig ml1a = new ControlEntryConfig("ML1a",
        new RichText("Rifles and combination guns, handguns and machine, sub-machine and volley guns"),
        new RichText("Rifles"), ml1, false, false);

    ControlEntryConfig ml1b = new ControlEntryConfig("ML1b", new RichText("Smooth-bore weapons"), null, ml1, false,
        false);

    ControlEntryConfig ml1c = new ControlEntryConfig("ML1c", new RichText("Weapons using caseless ammunition"), null,
        ml1, false, false);

    List<AnswerConfig> stage1Answers = new ArrayList<>();
    stage1Answers.add(new AnswerConfig("A1", "2", null, ml1a, 1, false));
    stage1Answers.add(new AnswerConfig("A2", "3", null, ml1b, 2, false));
    stage1Answers.add(new AnswerConfig("A3", "4", null, ml1c, 3, false));

    StageConfig stage1 = new StageConfig(STAGE_1_ID, "Question for the first stage", null,
        StageConfig.QuestionType.STANDARD, StageConfig.AnswerType.SELECT_ONE, null, ml1, stage1Answers);

    configMap.put(STAGE_1_ID, stage1);

    StageConfig stage2 = new StageConfig(STAGE_2_ID, null, null, StageConfig.QuestionType.DECONTROL,
        StageConfig.AnswerType.SELECT_MANY, STAGE_3_ID, ml1a, stage1Answers);

    configMap.put(STAGE_2_ID, stage2);

    StageConfig stage3 = new StageConfig(STAGE_3_ID, null, null, StageConfig.QuestionType.STANDARD,
        StageConfig.AnswerType.SELECT_ONE, null, ml1b, Collections.emptyList());

    configMap.put(STAGE_3_ID, stage3);

    StageConfig stage4 = new StageConfig(STAGE_4_ID, null, null, StageConfig.QuestionType.STANDARD,
        StageConfig.AnswerType.SELECT_MANY, null, ml1c, stage1Answers);

    configMap.put(STAGE_4_ID, stage4);
  }

  @Override
  public String getInitialStageId() {
    return STAGE_1_ID;
  }

  @Override
  public StageConfig getStageConfigForStageId(String stageId) {
    return configMap.get(stageId);
  }

  @Override
  public List<NoteConfig> getNotesForStageId(String stageId) {
    return Collections.emptyList();
  }

  @Override
  public List<ControlEntryConfig> getParentRatings(ControlEntryConfig controlEntryConfig) {
    return Collections.emptyList();
  }

  @Override
  public List<ControlEntryConfig> getChildRatings(ControlEntryConfig controlEntryConfig) {
    return Collections.emptyList();
  }
}
