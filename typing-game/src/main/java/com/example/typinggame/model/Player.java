package com.example.typinggame.model;

public class Player {
    private final String nickname;
    private final long durationMillis;
    private final int correctChars;

    public Player(String nickname, long durationMillis, int correctChars) {
        this.nickname = nickname;
        this.durationMillis = durationMillis;
        this.correctChars = correctChars;
    }

    public String getNickname() {
        return nickname;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public int getCorrectChars() {
        return correctChars;
    }
}
