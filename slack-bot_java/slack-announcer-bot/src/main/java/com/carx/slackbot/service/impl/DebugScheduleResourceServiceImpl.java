package com.carx.slackbot.service.impl;

import com.carx.slackbot.configs.properties.SlackBotProperties;
import com.carx.slackbot.models.EventMessageModel;
import com.carx.slackbot.repo.PublishedEventRepo;
import com.carx.slackbot.service.ScheduleResourceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

/**
 * @author Created by KarpuninVD on 18.01.2023
 */
@Primary
@Service
@Slf4j
@ConditionalOnProperty(name = "app.messageSource.debug", havingValue = "true")
@RequiredArgsConstructor
public class DebugScheduleResourceServiceImpl implements ScheduleResourceService {
    private final PublishedEventRepo repo;
    private final SlackBotProperties props;

    @PostConstruct
    public void init() {
        repo.clearAll();
    }

    @Override
    public List<EventMessageModel> getSlackBlockMessagesFromFile() {
        List<String> adminIds = props.getAdminIds();
        Random random = new Random();
        return List.of(
                new EventMessageModel(1, LocalDate.now(), LocalTime.now().plusSeconds(10), "place_1", LocalTime.now().plusSeconds(100),
                      List.of(adminIds.get(random.nextInt(adminIds.size())), adminIds.get(random.nextInt(adminIds.size()))), "Test Title 1", "http://test.ru"),
                new EventMessageModel(2, LocalDate.now(), LocalTime.now().plusSeconds(20), "place_1", LocalTime.now().plusSeconds(120),
                       null, "Test breakfast1", null),
                new EventMessageModel(3, LocalDate.now(), LocalTime.now().plusSeconds(30), "place_2",LocalTime.now().plusSeconds(150),
                        null, "Test breakfast2", null),
                new EventMessageModel(4, LocalDate.now(), LocalTime.now().plusSeconds(40), "place_3", LocalTime.now().plusSeconds(160),
                        null, "Test breakfast3", null),
                new EventMessageModel(5, LocalDate.now(), LocalTime.now().plusSeconds(50), "place_1", LocalTime.now().plusSeconds(200),
                        List.of(adminIds.get(random.nextInt(adminIds.size()))), "Test Title 2", "http://test.ru")
        );
    }

    @Override
    public boolean updateResourceFileIfNeeded() {
        return false;
    }
}
