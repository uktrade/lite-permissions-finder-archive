package triage.session;

import com.google.inject.Inject;
import components.cms.dao.JourneyDao;
import components.cms.dao.SessionDao;
import components.cms.dao.SessionStageDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class SessionServiceImpl implements SessionService {

  private static final List<Character> CODE_DIGITS = Collections.unmodifiableList(Arrays.asList(
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'));

  private final SessionDao sessionDao;
  private final SessionStageDao sessionStageDao;
  private final JourneyDao journeyDao;

  @Inject
  public SessionServiceImpl(SessionDao sessionDao, SessionStageDao sessionStageDao, JourneyDao journeyDao) {
    this.sessionDao = sessionDao;
    this.sessionStageDao = sessionStageDao;
    this.journeyDao = journeyDao;
  }

  @Override
  public TriageSession createNewSession() {
    String sessionId = UUID.randomUUID().toString();
    long journeyId = journeyDao.getJourneysByJourneyName("MILITARY").get(0).getId();
    String resumeCode = generateResumeCode();
    TriageSession triageSession = new TriageSession(sessionId, journeyId, resumeCode, null, null);
    sessionDao.insert(triageSession);
    return triageSession;
  }

  @Override
  public TriageSession getSessionById(String id) {
    return sessionDao.getSessionById(id);
  }

  @Override
  public TriageSession getSessionByResumeCode(String resumeCode) {
    return sessionDao.getSessionByResumeCode(resumeCode);
  }

  @Override
  public Set<String> getAnswersForStageId(String sessionId, String stageId) {
    SessionStage sessionStage = sessionStageDao.getSessionStage(sessionId, Long.parseLong(stageId));
    if (sessionStage != null) {
      return new HashSet<>(sessionStage.getAnswers());
    } else {
      return new HashSet<>();
    }
  }

  @Override
  public void saveAnswersForStageId(String sessionId, String stageId, Set<String> answerIds) {
    sessionStageDao.insert(new SessionStage(sessionId, Long.parseLong(stageId), new ArrayList<>(answerIds)));
  }

  @Override
  public String getStageId(String sessionId) {
    // TODO
    return null;
  }

  private String generateResumeCode() {
    StringBuilder sb = new StringBuilder();
    IntStream.range(0, 8).forEach(i -> sb.append(CODE_DIGITS.get(ThreadLocalRandom.current().nextInt(0, CODE_DIGITS.size()))));
    return sb.insert(4, "-").toString();
  }

}
