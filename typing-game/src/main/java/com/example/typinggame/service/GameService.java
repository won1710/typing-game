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
                    new RoundResult(0, (long) ROUND_SECONDS * 1000, false, 0, 0, 0));
        }

        // 점수 DESC 정렬
        List<Map.Entry<String, RoundResult>> ranked = new ArrayList<>(submissions.entrySet());
        ranked.sort((a, b) -> Double.compare(b.getValue().score, a.getValue().score));

        broadcast("SYSTEM", "══ 라운드 종료 ══", 0);

        String[] medals = {"1위", "2위", "3위"};
        for (int i = 0; i < ranked.size(); i++) {
            Map.Entry<String, RoundResult> e = ranked.get(i);
            RoundResult r = e.getValue();
            String medal = i < medals.length ? medals[i] : (i + 1) + "위";
            String line = r.submitted
                    ? String.format("%s %s  |  점수 %.0f  |  정확도 %.1f%%  |  %.0f 타/분  |  %.1f초",
                        medal, e.getKey(), r.score, r.accuracy, r.cpm, r.durationMillis / 1000.0)
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
        double accuracy = calcAccuracy(correct, currentSentence.length());
        double cpm = calcCpm(correct, durationMillis);
        double score = cpm * (accuracy / 100.0);

        submissions.put(nickname, new RoundResult(correct, durationMillis, true, accuracy, cpm, score));

        broadcast("SYSTEM",
                String.format("[%s] 제출  |  정확도 %.1f%%  |  %.0f 타/분", nickname, accuracy, cpm),
                getUserCount());
    }

    // ── 내부 유틸 ───────────────────────────────────────

    // 위치별 일치 글자 수 (첫 오류 이후도 계속 비교)
    private int countCorrect(String typed, String target) {
        int count = 0;
        int limit = Math.min(typed.length(), target.length());
        for (int i = 0; i < limit; i++) {
            if (typed.charAt(i) == target.charAt(i)) count++;
        }
        return count;
    }

    private double calcAccuracy(int correctChars, int targetLength) {
        if (targetLength == 0) return 0;
        return Math.min((double) correctChars / targetLength * 100, 100.0);
    }

    // 글자/분 (characters per minute)
    private double calcCpm(int correctChars, long durationMillis) {
        if (durationMillis == 0) return 0;
        return (double) correctChars / durationMillis * 60_000;
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
        final double accuracy;  // %
        final double cpm;       // 타/분
        final double score;     // cpm × (accuracy/100)

        RoundResult(int correctChars, long durationMillis, boolean submitted,
                    double accuracy, double cpm, double score) {
            this.correctChars = correctChars;
            this.durationMillis = durationMillis;
            this.submitted = submitted;
            this.accuracy = accuracy;
            this.cpm = cpm;
            this.score = score;
        }
    }
}
