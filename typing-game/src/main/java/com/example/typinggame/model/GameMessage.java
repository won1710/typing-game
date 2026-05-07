package com.example.typinggame.model;

public class GameMessage {
    private String type;     // SYSTEM, GAME, TIMER, WARN, RANK
    private String text;
    private String nickname;
    private int userCount;
    private int secondsLeft; // GAME 메시지에서 현재 라운드 남은 시간

    public GameMessage() {}

    public GameMessage(String type, String text, int userCount) {
        this.type = type;
        this.text = text;
        this.userCount = userCount;
    }

    public GameMessage(String type, String text, int userCount, int secondsLeft) {
        this.type = type;
        this.text = text;
        this.userCount = userCount;
        this.secondsLeft = secondsLeft;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public int getUserCount() { return userCount; }
    public void setUserCount(int userCount) { this.userCount = userCount; }
    public int getSecondsLeft() { return secondsLeft; }
    public void setSecondsLeft(int secondsLeft) { this.secondsLeft = secondsLeft; }
}
