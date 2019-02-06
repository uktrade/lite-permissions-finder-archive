package triage.session;

import com.google.inject.Inject;
import components.cms.dao.SessionDao;
import components.cms.dao.SessionStageDao;
import components.cms.dao.SpreadsheetVersionDao;
import lombok.AllArgsConstructor;
import triage.config.JourneyConfigService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class SessionServiceImpl implements SessionService {

  private static final List<Character> CODE_DIGITS = Collections.unmodifiableList(Arrays.asList(
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'));

  private final SessionDao sessionDao;
  private final SessionStageDao sessionStageDao;
  private final SpreadsheetVersionDao spreadsheetVersionDao;

  @Override
  public TriageSession createNewSession() {
    String sessionId = UUID.randomUUID().toString();
    String resumeCode = generateResumeCode();
    long spreadsheetVersionId = spreadsheetVersionDao.getLatestSpreadsheetVersion().getId();
    TriageSession triageSession = new TriageSession(sessionId, null, resumeCode, spreadsheetVersionId, null,
      Collections.EMPTY_SET, Collections.EMPTY_SET);
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
  public Set<String> getAnswerIdsForStageId(String sessionId, String stageId) {
    SessionStage sessionStage = sessionStageDao.getSessionStage(sessionId, Long.parseLong(stageId));
    if (sessionStage != null) {
      return new HashSet<>(sessionStage.getAnswerIds());
    } else {
      return new HashSet<>();
    }
  }

  @Override
  public void saveAnswerIdsForStageId(String sessionId, String stageId, Set<String> answerIds) {
    sessionStageDao.insert(new SessionStage(sessionId, Long.parseLong(stageId), new ArrayList<>(answerIds)));
  }

  @Override
  public void updateLastStageId(String sessionId, String lastStageId) {
    sessionDao.updateLastStageId(sessionId, Long.parseLong(lastStageId));
  }

  @Override
  public void addDecontrolledCodeFound(String sessionId, String controlCode) {
    TriageSession session = sessionDao.getSessionById(sessionId);
    Set<String> decontrolCodesFound =  session.getDecontrolledCodesFound();
    if (decontrolCodesFound.add(controlCode)) {
      sessionDao.updateDecontrolCodesFound(sessionId, new ArrayList<>(decontrolCodesFound));
    }
  }

  @Override
  public void addControlEntryIdsToVerifyDecontrolledStatus(String sessionId, Set<String> controlEntryIds) {
    TriageSession session = sessionDao.getSessionById(sessionId);
    Set<String> controlEntryIdsToVeryifyDecontrolledStatus = session.getControlEntryIdsToVerifyDecontrolledStatus();
    if (controlEntryIdsToVeryifyDecontrolledStatus.addAll(controlEntryIds)) {
      sessionDao.updateControlEntryIdsToVerifyDecontrolledStatus(sessionId, new ArrayList<>(controlEntryIdsToVeryifyDecontrolledStatus));
    }
  }

  @Override
  public Optional<String> getAndRemoveControlEntryIdForDecontrolledStatusVerification(String sessionId) {
    TriageSession session = sessionDao.getSessionById(sessionId);
    Set<String> controlEntryIdsToVeryifyDecontrolledStatus = session.getControlEntryIdsToVerifyDecontrolledStatus();
    if (controlEntryIdsToVeryifyDecontrolledStatus.isEmpty()) {
      return Optional.empty();
    } else {
      Iterator<String> it = controlEntryIdsToVeryifyDecontrolledStatus.iterator();
      String controlEntryId = it.next();
      it.remove();
      sessionDao.updateControlEntryIdsToVerifyDecontrolledStatus(sessionId, new ArrayList<>(controlEntryIdsToVeryifyDecontrolledStatus));
      return Optional.of(controlEntryId);
    }
  }

  private String generateResumeCode() {
    StringBuilder sb = new StringBuilder();
    IntStream.range(0, 8).forEach(i -> sb.append(CODE_DIGITS.get(ThreadLocalRandom.current().nextInt(0, CODE_DIGITS.size()))));
    return sb.insert(4, "-").toString();
  }

}
