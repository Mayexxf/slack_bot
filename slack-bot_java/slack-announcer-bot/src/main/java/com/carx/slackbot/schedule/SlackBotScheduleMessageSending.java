package com.carx.slackbot.schedule;

import com.carx.slackbot.configs.properties.AppProperties;
import com.carx.slackbot.configs.properties.SlackBotProperties;
import com.carx.slackbot.models.EventMessageModel;
import com.carx.slackbot.service.ScheduleResourceService;
import com.carx.slackbot.service.SlackBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.carx.slackbot.utils.DateTimeUtils.nowDate;
import static com.carx.slackbot.utils.DateTimeUtils.nowTime;

/**
 * @author Created by KarpuninVD on 14.12.2022
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlackBotScheduleMessageSending {

    private final static int SECONDS_FIXED_DELAY = 10;
    private final SlackBotService botService;
    private final ScheduleResourceService resourceService;
    //    private final PublishedEventRepo publishedEventRepo;
    private final AppProperties appProperties;
    private final SlackBotProperties slackBotProperties;
    private final Map<AppProperties.Inform, List<EventMessageModel>> messagesToPublic = new HashMap<>();
    private boolean nothingToPost = false;

    private static boolean beforeNow(EventMessageModel event) {
        if (event.getDate().isAfter(nowDate())) {
            return true;
        } else {
            return event.getDate().equals(nowDate()) &&
                    (event.getBeginTime().isAfter(nowTime()) || event.getBeginTime().equals(nowTime()));
        }
    }

    //todo redo
    @Scheduled(fixedRate = SECONDS_FIXED_DELAY, timeUnit = TimeUnit.SECONDS)
    public void checkScheduleForMessages() {
        log.debug("Tick of scheduler");
        if (resourceService.updateResourceFileIfNeeded()) {
            messagesToPublic.clear();
            nothingToPost = false;
        }

        loadMessages();

        if (messagesToPublic.isEmpty()) {
            nothingToPost = true;
            return;
        }

        Map<AppProperties.Inform, List<EventMessageModel>> copyMap = new HashMap<>(messagesToPublic);
        copyMap.forEach((key, value) -> {
            ArrayList<EventMessageModel> copy = new ArrayList<>(value);
            copy.stream()
                    .filter(msg -> msg.getDate().isEqual(nowDate()) &&
                            Math.abs(msg.getBeginTime().minusMinutes(key.getTime()).toSecondOfDay() -
                                    nowTime().toSecondOfDay()) <=
                                    SECONDS_FIXED_DELAY)
//                    .filter(event -> !publishedEventRepo.hasByMessageId(event.getId(), key))
                    .forEach(msg -> sendMessages(msg, key));
        });

        botService.updateViewIfChanged();
    }

    private void loadMessages() {
        if (messagesToPublic.isEmpty() && !nothingToPost) {
            resourceService.getSlackBlockMessagesFromFile().stream()
                    .filter(SlackBotScheduleMessageSending::beforeNow)
                    .forEach(event -> appProperties.getInform().forEach(inform -> {
                        final EventMessageModel eventMessageModel = new EventMessageModel(event);
                        eventMessageModel.setBeginTime(event.getBeginTime().minusMinutes(inform.getTime()));
                        if (beforeNow(eventMessageModel)) {
                            Optional.ofNullable(messagesToPublic.get(inform))
                                    .orElseGet(() -> {
                                        messagesToPublic.put(inform, new ArrayList<>());
                                        return messagesToPublic.get(inform);
                                    })
                                    .add(event);
                        }
                    }));
            log.info("{} messages will be published", messagesToPublic.values().stream().mapToInt(List::size).sum());
        }
    }

    private void sendMessages(EventMessageModel msg, AppProperties.Inform inform) {
        botService.sendMessageToAllUsers(msg, inform);

        if (inform.getTime() == 0) {
            botService.publishHomePage(msg, inform);
            botService.sendMessageToChannel(slackBotProperties.getChannelStart(), msg, inform);
        }

        final List<EventMessageModel> eventMessageModels = messagesToPublic.get(inform);
        Optional.ofNullable(eventMessageModels).ifPresent($ -> $.remove(msg));
        if (eventMessageModels == null || eventMessageModels.isEmpty()) {
            messagesToPublic.remove(inform);
        }
//        publishedEventRepo.save(msg.getId(), inform);
    }
}
