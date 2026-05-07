package com.example.slackscheduler.schedule;

import com.example.slackscheduler.service.SlackService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class SlackSchedule {

    private final SlackService slackService;

    public SlackSchedule(SlackService slackService) {
        this.slackService = slackService;
    }

    @Scheduled(cron = "0 30 7 * * *")
    public void sendMorningMessage() {
        slackService.sendMessage("좋은 아침입니다! 오늘도 좋은 하루 되세요.");
    }
}
