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
            "가는 말이 고와야 오는 말이 곱다",
            "가는 날이 장날이다",
            "가랑비에 옷 젖는 줄 모른다",
            "가랑잎이 솔잎더러 바스락거린다고 한다",
            "가재는 게 편이다",
            "갈수록 태산이다",
            "같은 값이면 다홍치마다",
            "개구리 올챙이 적 생각 못 한다",
            "개밥에 도토리다",
            "고래 싸움에 새우 등 터진다",
            "고생 끝에 낙이 온다",
            "공든 탑이 무너지랴",
            "구르는 돌에는 이끼가 끼지 않는다",
            "구슬이 서 말이라도 꿰어야 보배다",
            "금강산도 식후경이다",
            "낙숫물이 댓돌을 뚫는다",
            "남의 떡이 커 보인다",
            "내 코가 석 자다",
            "누워서 침 뱉기다",
            "눈 가리고 아웅 한다",
            "달면 삼키고 쓰면 뱉는다",
            "닭 잡아먹고 오리발 내놓는다",
            "도둑이 제 발 저리다",
            "도마 위에 오른 고기다",
            "돌다리도 두드려 보고 건너라",
            "되로 주고 말로 받는다",
            "등잔 밑이 어둡다",
            "말 한 마디에 천 냥 빚도 갚는다",
            "먼 사촌보다 가까운 이웃이 낫다",
            "모로 가도 서울만 가면 된다",
            "목마른 사람이 우물 판다",
            "못 먹는 감 찔러나 본다",
            "물에 빠진 사람 지푸라기라도 잡는다",
            "미꾸라지 한 마리가 온 웅덩이를 흐린다",
            "바늘 도둑이 소도둑 된다",
            "발 없는 말이 천 리 간다",
            "배보다 배꼽이 더 크다",
            "백지장도 맞들면 낫다",
            "뱁새가 황새 걸음을 걸으면 가랑이 찢어진다",
            "번갯불에 콩 구워 먹는다",
            "벼 이삭은 익을수록 고개를 숙인다",
            "병 주고 약 준다",
            "보기 좋은 떡이 먹기도 좋다",
            "부뚜막의 소금도 집어넣어야 짜다",
            "빈 수레가 요란하다",
            "사공이 많으면 배가 산으로 간다",
            "사람은 죽어서 이름을 남기고 범은 죽어서 가죽을 남긴다",
            "세 살 버릇 여든까지 간다",
            "소 잃고 외양간 고친다",
            "소문난 잔치에 먹을 것 없다",
            "손바닥도 마주쳐야 소리가 난다",
            "쇠귀에 경 읽기다",
            "수박 겉 핥기다",
            "시작이 반이다",
            "싸움에서 지고 소송에서 이긴다",
            "아니 땐 굴뚝에 연기 나랴",
            "아닌 밤중에 홍두깨다",
            "열 번 찍어 안 넘어가는 나무 없다",
            "우물 안 개구리다",
            "원숭이도 나무에서 떨어진다",
            "윗물이 맑아야 아랫물이 맑다",
            "이 없으면 잇몸으로 살아간다",
            "작은 고추가 맵다",
            "재주는 곰이 넘고 돈은 주인이 챙긴다",
            "종로에서 뺨 맞고 한강에서 눈 흘긴다",
            "쥐구멍에도 볕 들 날 있다",
            "지렁이도 밟으면 꿈틀한다",
            "천 리 길도 한 걸음부터다",
            "콩 심은 데 콩 나고 팥 심은 데 팥 난다",
            "티끌 모아 태산이다",
            "하늘이 무너져도 솟아날 구멍이 있다",
            "하룻강아지 범 무서운 줄 모른다",
            "핑계 없는 무덤 없다",
            "호랑이도 제 말 하면 온다",
            "호미로 막을 것을 가래로 막는다",
            "가는 토끼 잡으려다 잡은 토끼 놓친다",
            "간에 기별도 안 간다",
            "강 건너 불구경이다",
            "개천에서 용 난다",
            "고양이 앞의 쥐다",
            "굴러온 돌이 박힌 돌 뺀다",
            "그림의 떡이다",
            "기왕이면 다홍치마다",
            "까마귀 날자 배 떨어진다",
            "꿩 먹고 알 먹는다",
            "나무를 보고 숲을 보지 못한다",
            "낫 놓고 기역자도 모른다",
            "남의 잔치에 감 놓아라 배 놓아라 한다",
            "늦게 배운 도둑이 날 새는 줄 모른다",
            "다 된 죽에 코 빠뜨린다",
            "도토리 키 재기다",
            "돌 위에서 삼 년이면 돌도 더워진다",
            "땅 짚고 헤엄치기다",
            "떡 줄 사람은 꿈도 안 꾸는데 김칫국부터 마신다",
            "뜻이 있는 곳에 길이 있다",
            "말이 씨가 된다",
            "맞는 거지가 없다",
            "믿는 도끼에 발등 찍힌다",
            "바늘 가는 데 실 간다",
            "서당 개 삼 년이면 풍월을 읊는다"
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
                broadcast(type, secondsLeft + "초 남았습니다", secondsLeft);
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

        broadcast("SYSTEM", BREAK_SECONDS + "초 후 다음 라운드를 시작합니다", 0);
        scheduler.schedule(this::tryStartRound, BREAK_SECONDS, TimeUnit.SECONDS);
    }

    // ── 플레이어 제출 ───────────────────────────────────

    public void submitAnswer(String nickname, String typedText) {
        if (!roundActive) return;
        RoundResult prev = submissions.get(nickname);
        if (prev != null && prev.submitted) return;

        long durationMillis = System.currentTimeMillis() - roundStartMillis;
        int correct = countCorrect(typedText, currentSentence);
        int correctStrokes = countCorrectKeystrokes(typedText, currentSentence);
        double accuracy = calcAccuracy(correct, currentSentence.length());
        double cpm = calcCpm(correctStrokes, durationMillis);
        double score = cpm * (accuracy / 100.0);

        submissions.put(nickname, new RoundResult(correct, durationMillis, true, accuracy, cpm, score));

        broadcast("SYSTEM",
                String.format("[%s] %s  |  정확도 %.1f%%  |  %.0f 타/분", nickname, typedText, accuracy, cpm),
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

    // 맞은 글자의 실제 키 입력 수 (한글 자모 분해 기준)
    private int countCorrectKeystrokes(String typed, String target) {
        int strokes = 0;
        int limit = Math.min(typed.length(), target.length());
        for (int i = 0; i < limit; i++) {
            if (typed.charAt(i) == target.charAt(i)) {
                strokes += keystrokesForChar(target.charAt(i));
            }
        }
        return strokes;
    }

    // 두벌식 기준: 한글 음절 → 초성(1) + 중성(1~2) + 종성(0~2)
    private static final int[] JUNGSEONG_STROKES = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, // ㅏ~ㅗ
        2, 2, 2,                     // ㅘ, ㅙ, ㅚ
        1, 1,                        // ㅛ, ㅜ
        2, 2, 2,                     // ㅝ, ㅞ, ㅟ
        1, 1,                        // ㅠ, ㅡ
        2,                           // ㅢ
        1                            // ㅣ
    };
    private static final int[] JONGSEONG_STROKES = {
        0, 1, 1, 2, 1, 2, 2, 1, 1,  // (없음), ㄱ, ㄲ, ㄳ, ㄴ, ㄵ, ㄶ, ㄷ, ㄹ
        2, 2, 2, 2, 2, 2, 2,         // ㄺ~ㅀ
        1, 1, 2,                     // ㅁ, ㅂ, ㅄ
        1, 1, 1, 1, 1, 1, 1, 1, 1   // ㅅ~ㅎ
    };

    private int keystrokesForChar(char c) {
        if (c < 0xAC00 || c > 0xD7A3) return 1;
        int code = c - 0xAC00;
        int jungseong = (code % (21 * 28)) / 28;
        int jongseong = code % 28;
        return 1 + JUNGSEONG_STROKES[jungseong] + JONGSEONG_STROKES[jongseong];
    }

    private double calcAccuracy(int correctChars, int targetLength) {
        if (targetLength == 0) return 0;
        return Math.min((double) correctChars / targetLength * 100, 100.0);
    }

    // 타수/분 (두벌식 자모 기준 keystrokes per minute)
    private double calcCpm(int correctStrokes, long durationMillis) {
        if (durationMillis == 0) return 0;
        return (double) correctStrokes / durationMillis * 60_000;
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
