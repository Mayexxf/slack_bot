package com.carx.slackbot.service.impl;

import com.carx.slackbot.configs.properties.AppProperties;
import com.carx.slackbot.configs.properties.SlackBotProperties;
import com.carx.slackbot.enums.AdminMessageType;
import com.carx.slackbot.models.EventMessageModel;
import com.carx.slackbot.service.SlackBotService;
import com.carx.slackbot.service.SlackUsersListService;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsInfoResponse;
import com.slack.api.model.User;
import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.view.View;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.carx.slackbot.utils.DateTimeUtils.nowTime;

/**
 * @author Created by KarpuninVD on 14.12.2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlackBotServiceImpl implements SlackBotService {

    private final static String DEFAULT_USER_ID = "USLACKBOT";
    private final SlackBotProperties botProps;
    private final ConversionService conversionService;
    private final SlackUsersListService slackUsersListService;
    private final List<EventMessageModel> currentEvents = new ArrayList<>();
    private MethodsClient client;
    private String startChannelName;

    @PostConstruct
    public void init() {
        this.client = Slack.getInstance().methods();
        try {
            slackUsersListService.getAllUsers();
            startChannelName = getChannelStartName();
        } catch (SlackApiException | IOException e) {
            log.error("Cannot get users", e);
            sendMessageToAdmins("Cannot get users. ERROR: " + e.getMessage(), AdminMessageType.ERROR);
        }
    }

    @Override
    public void sendMessageToAllUsers(EventMessageModel message, AppProperties.Inform inform) {
        try {
            slackUsersListService.getAllUsers().stream()
                    .filter(user -> !user.isBot())
                    .map(User::getId)
                    .filter(id -> !id.equals(DEFAULT_USER_ID))
                    .forEach(id -> {
                        try {
                            final List<LayoutBlock> layoutBlock = new ArrayList<>(
                                    List.of(new SectionBlock(new MarkdownTextObject("*" + inform.getMessage() + "*", false),
                                                    "block1",
                                                    null,
                                                    null),
                                            new DividerBlock())
                            );
                            layoutBlock.addAll(conversionService.convert(message, List.class));

                            if (inform.getTime() == 0 && message.getSpeakersId() != null && !message.getSpeakersId().isEmpty()) {
                                layoutBlock.add(layoutBlock.size() - 1,
                                        new ContextBlock(List.of(new MarkdownTextObject(
                                                String.format("Вопросы спикеру задавайте в обсуждении %s",
                                                        startChannelName.isEmpty() ? "" : "#" + startChannelName), false)),
                                                "tooltip_" + message.getSpeakersId()));
                            }

                            client.chatPostMessage(msg -> msg.token(botProps.getToken())
                                    .channel(id)
                                    .text("Сообщение о мероприятии")
                                    .blocks(layoutBlock)
                            );
                        } catch (IOException | SlackApiException e) {
                            throw new RuntimeException(e);
                        }
                    });
            log.info("Messages are sent");
        } catch (Exception ex) {
            log.error("Cannot get users", ex);
            sendMessageToAdmins("Cannot get users. ERROR: " + ex.getMessage(), AdminMessageType.ERROR);
        }
    }

    @Override
    public void sendMessageToChannel(String channelId, EventMessageModel message, AppProperties.Inform inform) {
        try {
            final LinkedList<LayoutBlock> layoutBlock = new LinkedList<>(
                    List.of(new SectionBlock(new MarkdownTextObject("*" + inform.getMessage() + "*", false),
                                    "block1",
                                    null,
                                    null),
                            new DividerBlock())
            );
            layoutBlock.addAll(conversionService.convert(message, List.class));

            final boolean isStart = inform.getTime() == 0 && Objects.equals(channelId, botProps.getChannelStart()) &&
                    message.getSpeakersId() != null && !message.getSpeakersId().isEmpty();

            if (isStart) {
                layoutBlock.add(layoutBlock.size() - 1,
                        new ContextBlock(List.of(new MarkdownTextObject("Вопросы спикеру задавайте в обсуждении", false)),
                                "tooltip_" + message.getSpeakersId()));
            }

            ChatPostMessageResponse response = client.chatPostMessage(msg -> msg.token(botProps.getToken())
                    .channel(channelId)
                    .text("Сообщение о мероприятии")
                    .blocks(layoutBlock)
            );

            if (isStart) {
                client.chatPostMessage(msg -> msg.channel(response.getChannel())
                        .token(botProps.getToken())
                        .text("Пожалуйста, задавайте ваши вопросы здесь.")
                        .threadTs(response.getTs())
                );
            }

        } catch (IOException | SlackApiException e) {
            sendMessageToAdmins("Cannot send message to channel" + channelId + ". ERROR: " + e.getMessage(),
                    AdminMessageType.ERROR);
        }
    }


    @Override
    public void publishHomePage(EventMessageModel message, AppProperties.Inform inform) {
        currentEvents.add(message);
        updateView();
    }

    @Override
    public void updateViewIfChanged() {
        final List<EventMessageModel> eventMessageModels = currentEvents.stream()
                .filter($ -> $.getEndTime().isBefore(nowTime())).toList();
        if (!eventMessageModels.isEmpty()) {
            currentEvents.removeAll(eventMessageModels);
            updateView();
        }
    }

    private void updateView() {
        final List<LayoutBlock> layoutBlock = new ArrayList<>(
                List.of(new HeaderBlock("header", new PlainTextObject("Текущие мероприятия", false)),
                        new DividerBlock())
        );

        currentEvents.forEach(event -> layoutBlock.addAll(conversionService.convert(event, List.class)));

        try {
            slackUsersListService.getAllUsers().forEach(user -> {
                try {
                    client.viewsPublish(msg -> msg.token(botProps.getToken())
                            .userId(user.getId())
                            .view(View.builder()
                                    .blocks(layoutBlock)
                                    .type("home")
                                    .build())
                    );
                } catch (IOException | SlackApiException e) {
                    sendMessageToAdmins("Cannot publish view. ERROR: " + e.getMessage(),
                            AdminMessageType.ERROR);
                }
            });
        } catch (SlackApiException | IOException e) {
            log.error("Cannot get users", e);
            sendMessageToAdmins("Cannot get users. ERROR: " + e.getMessage(), AdminMessageType.ERROR);
        }
    }

    @Override
    public void sendMessageToAdmins(String message, AdminMessageType messageType) {
        botProps.getAdminIds().forEach(id -> {
                    try {
                        client.chatPostMessage(msg -> msg.token(botProps.getToken())
                                .channel(id)
                                .text("ADMIN MESSAGE - " + messageType.name())
                                .blocks(List.of(new SectionBlock(
                                        new MarkdownTextObject(String.format(":%s: %s", messageType.getEmoji(), message), false),
                                        "blockId", null, null)))
                        );
                    } catch (IOException | SlackApiException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private String getChannelStartName() {
        final ConversationsInfoResponse channel;
        try {
            channel = client.conversationsInfo(info -> info.channel(botProps.getChannelStart()).token(botProps.getToken()));
            return channel.getChannel().getName();
        } catch (IOException | SlackApiException e) {
            log.warn("Cannot get start channel name");
            sendMessageToAdmins("Cannot get start channel name", AdminMessageType.WARN);
            return "";
        }
    }
}
