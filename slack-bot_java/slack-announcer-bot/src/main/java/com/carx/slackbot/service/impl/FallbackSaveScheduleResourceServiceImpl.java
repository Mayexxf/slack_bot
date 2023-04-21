package com.carx.slackbot.service.impl;

import com.carx.slackbot.enums.AdminMessageType;
import com.carx.slackbot.models.EventMessageModel;
import com.carx.slackbot.service.ScheduleResourceService;
import com.carx.slackbot.service.SlackBotService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by KarpuninVD on 18.01.2023
 */
@Primary
@Service
@Slf4j
@ConditionalOnProperty(name = "app.messageSource.debug", havingValue = "false")
public class FallbackSaveScheduleResourceServiceImpl implements ScheduleResourceService {

    private final ScheduleResourceService mainService;
    private final ScheduleResourceService reserveService;
    private final SlackBotService botService;
    private ScheduleResourceService activeService;
    private ExecutorService executorService;

    @Autowired
    public FallbackSaveScheduleResourceServiceImpl(
            @Qualifier("yandex-disk") ScheduleResourceService mainService,
            @Qualifier("local-file") ScheduleResourceService reserveService,
            SlackBotService botService) {
        this.activeService = mainService;
        this.mainService = mainService;
        this.reserveService = reserveService;
        this.botService = botService;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public List<EventMessageModel> getSlackBlockMessagesFromFile() {
        try {
            log.info("Trying to get events from service <{}>", activeService.getClass().getSimpleName());
            return activeService.getSlackBlockMessagesFromFile();
        } catch (Exception ex) {
            log.warn("Service <{}> is works bad.", activeService.getClass().getSimpleName());
            botService.sendMessageToAdmins("Service is works bad. ERROR: " + ex.getMessage(), AdminMessageType.WARN);
            switchToReserveForSeconds(30);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean updateResourceFileIfNeeded() {
        try {
            return activeService.updateResourceFileIfNeeded();
        } catch (Exception e) {
            switchToReserveForSeconds(30);
            return true;
        }
    }

    private void switchToReserveForSeconds(int seconds) {
        if (activeService.equals(mainService)) {
            log.info("Active service is switched from main to reserve");
            activeService = reserveService;
            executorService.execute(createSwitchToReserveTask(seconds));
        }
    }

    @NotNull
    private Runnable createSwitchToReserveTask(int seconds) {
        return () -> {
            try {
                TimeUnit.SECONDS.sleep(seconds);
                activeService = mainService;
                log.info("Active service is switched from reserve to main");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }
}
