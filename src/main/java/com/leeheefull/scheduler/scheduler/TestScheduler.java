package com.leeheefull.scheduler.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class TestScheduler {

    /**
     * 1분 마다
     */
    @Scheduled(cron = "1 * * * * *")
    public void scheduleByCron() {
        log.info("[use cron]: {}", new Date(System.currentTimeMillis()));
    }

    /**
     * 3초 마다
     */
    @Scheduled(fixedDelay = 3000)
    public void scheduleByFixedDelay() {
        log.info("[use fixedDelay]: {}", new Date(System.currentTimeMillis()));
    }

}
