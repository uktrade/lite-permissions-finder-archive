package triage.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class SessionServiceMockImpl implements SessionService {

  private List<TriageSession> triageSessions = new ArrayList<>();
  private Map<String, Map<String, Set<String>>> sessionIdToStageIdToAnswers = new HashMap<>();
  private Map<String, String> sessionIdToStageId = new HashMap<>();

  private static final List<Character> CODE_DIGITS = Collections.unmodifiableList(Arrays.asList(
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'));

  @Override
  public TriageSession createNewSession() {
    String sessionId = UUID.randomUUID().toString();
    String resumeCode = generateResumeCode();
    TriageSession triageSession = new TriageSession(sessionId, resumeCode);
    triageSessions.add(triageSession);
    return triageSession;
  }

  @Override
  public TriageSession getSessionById(String id) {
    return triageSessions.stream()
        .filter(triageSession -> triageSession.getSessionId().equals(id))
        .findFirst()
        .orElse(null);
  }

  @Override
  public TriageSession getSessionByResumeCode(String resumeCode) {
    return triageSessions.stream()
        .filter(triageSession -> triageSession.getResumeCode().equals(resumeCode))
        .findFirst()
        .orElse(null);
  }

  @Override
  public Set<String> getAnswersForStageId(String sessionId, String stageId) {
    return sessionIdToStageIdToAnswers.getOrDefault(sessionId, new HashMap<>()).getOrDefault(stageId, new HashSet<>());
  }

  @Override
  public void saveAnswersForStageId(String sessionId, String stageId, Set<String> answerIds) {
    Map<String, Set<String>> stageIdToAnswers = sessionIdToStageIdToAnswers.getOrDefault(sessionId, new HashMap<>());
    stageIdToAnswers.put(stageId, answerIds);
    sessionIdToStageIdToAnswers.put(sessionId, stageIdToAnswers);
    sessionIdToStageId.put(sessionId, stageId);
  }

  @Override
  public String getStageId(String sessionId) {
    return sessionIdToStageId.get(sessionId);
  }

  @Override
  public void saveNlrOutcome(String sessionId, TriageSession.NlrType nlrType, String pageHtml) {

  }

  @Override
  public void saveOgelOutcome(String sessionId, String ogelRegistrationId) {

  }

  private String generateResumeCode() {
    StringBuilder sb = new StringBuilder();
    IntStream.range(0, 8).forEach(i -> sb.append(CODE_DIGITS.get(ThreadLocalRandom.current().nextInt(0, CODE_DIGITS.size()))));
    return sb.insert(4, "-").toString();
  }

}
