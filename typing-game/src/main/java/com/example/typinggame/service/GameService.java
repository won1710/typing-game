package com.example.typinggame.service;

import com.example.typinggame.model.GameMessage;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class GameService {

    private static final int ROUND_SECONDS = 20;
    private static final int BREAK_SECONDS = 5;

    private static final List<String> SENTENCES = List.of(
            "Spring Boot로 작성된 타자 게임에 오신 것을 환영합니다.",
            "빠른 손가락이 승리를 만들어낸다.",
            "열심히 연습하면 반드시 실력이 느는 법이다.",
            "오늘 하루도 최선을 다해 즐겁게 살아가자.",
            "하늘은 스스로 돕는 자를 돕는다고 했다.",
            "작은 노력이 모여 결국 큰 결과를 만들어낸다.",
            "컴퓨터 앞에 앉아 코드를 두드리는 개발자들.",
            "타자 연습은 개발자의 기본 소양 중 하나이다.",
            "오늘 만든 코드가 내일의 기적을 만들어낼지도 모른다.",
            "키보드를 빠르게 치는 것도 하나의 재능이다."
    );

    private final SimpMessagingTemplate messaging;
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    private final Random random = new Random();

    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();
    private final Map<String, RoundResult> submissions = new ConcurrentHashMap<>();

    private volatile String currentSentence;
    private volatile long roundStartMillis;
    private volatile int secondsLeft = 0;
    private volatile boolean roundActive = false;
    private ScheduledFuture<?> tickTask;

    public GameService(@Lazy SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    // ── 사용자 관리 ────────────────────────────────────────

    public synchronized void addUser(String nickname) {
        activeUsers.add(nickname);
        if (!roundActive) {
            scheduler.schedule(this::tryStartRound, 2, TimeUnit.SECONDS);
        }
    }

    public void removeUser(String nickname) {
        activeUsers.remove(nickname);
    }

    public int getUserCount() {
        return activeUsers.size();
    }

    public String getCurrentSentence() {
        return currentSentence;
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }

    // ── 게임 루프 ────────────────────────────────────────

    private synchronized void tryStartRound() {
        if (roundActive || activeUsers.isEmpty()) return;
        startNewRound();
    }

    private void startNewRound() {
        roundActive = true;
        currentSentence = SENTENCES.get(random.nextInt(SENTENCES.size()));
        submissions.clear();
        secondsLeft = ROUND_SECONDS;
        roundStartMillis = System.currentTimeMillis();

        broadcast("SYSTEM", "══════════════════════════════════════════", ROUND_SECONDS);
        broadcast("GAME", currentSentence, ROUND_SECONDS);

        tickTask = scheduler.scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.SECONDS);
    }

    private void tick() {
        try {
            secondsLeft--;

            if (secondsLeft > 0 && secondsLeft <= 10) {
                String type = secondsLeft <= 5 ? "WARN" : "TIMER";
                broadcast(type, secondsLeft + "초 남았습니다.", secondsLeft);
            }

            if (secondsLeft <= 0) {
                if (tickTask != null) tickTask.cancel(false);
                endRound();
            }
        } catch (Exception e) {
            if (tickTask != null) tickTask.cancel(false);
        }
    }

    private void endRound() {
        roundActive = false;

        // 미제출자는 0점 처리
        for (String user : activeUsers) {
            submissions.putIfAbsent(user,
                    new RoundResult(0, (long) ROUND_SECONDS * 1000, false));
        }

        // 정확도 DESC, 시간 ASC 정렬
        List<Map.Entry<String, RoundResult>> ranked = new ArrayList<>(submissions.entrySet());
        ranked.sort((a, b) -> {
            int cmp = Integer.compare(b.getValue().correctChars, a.getValue().correctChars);
            return cmp != 0 ? cmp
                    : Long.compare(a.getValue().durationMillis, b.getValue().durationMillis);
        });

        broadcast("SYSTEM", "══ 라운드 종료 ══", 0);

        String[] medals = {"🥇", "🥈", "🥉"};
        for (int i = 0; i < ranked.size(); i++) {
            Map.Entry<String, RoundResult> e = ranked.get(i);
            RoundResult r = e.getValue();
            String medal = i < medals.length ? medals[i] : (i + 1) + "위";
            String line = r.submitted
                    ? medal + " " + e.getKey() + "  |  " + r.correctChars + "자  |  "
                      + String.format("%.2f", r.durationMillis / 1000.0) + "초"
                    : medal + " " + e.getKey() + "  |  미제출";
            broadcast("RANK", line, 0);
        }

        broadcast("SYSTEM", BREAK_SECONDS + "초 후 다음 라운드를 시작합니다.", 0);
        scheduler.schedule(this::tryStartRound, BREAK_SECONDS, TimeUnit.SECONDS);
    }

    // ── 플레이어 제출 ───────────────────────────────────

    public void submitAnswer(String nickname, String typedText) {
        if (!roundActive) return;
        RoundResult prev = submissions.get(nickname);
        if (prev != null && prev.submitted) return;

        long durationMillis = System.currentTimeMillis() - roundStartMillis;
        int correct = countCorrect(typedText, currentSentence);
        submissions.put(nickname, new RoundResult(correct, durationMillis, true));

        broadcast("SYSTEM",
                "[" + nickname + "] 제출 완료  |  " + correct + "자 정확",
                getUserCount());
    }

    // ── 내부 유틸 ───────────────────────────────────────

    private int countCorrect(String typed, String target) {
        int count = 0;
        int limit = Math.min(typed.length(), target.length());
        for (int i = 0; i < limit; i++) {
            if (typed.charAt(i) == target.charAt(i)) count++;
            else break;
        }
        return count;
    }

    private void broadcast(String type, String text, int sLeft) {
        messaging.convertAndSend("/topic/chat",
                new GameMessage(type, text, activeUsers.size(), sLeft));
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    // ── 내부 모델 ───────────────────────────────────────

    public static class RoundResult {
        final int correctChars;
        final long durationMillis;
        final boolean submitted;

        RoundResult(int correctChars, long durationMillis, boolean submitted) {
            this.correctChars = correctChars;
            this.durationMillis = durationMillis;
            this.submitted = submitted;
        }
    }
}
