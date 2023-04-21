package com.carx.slackbot.service;

import com.carx.slackbot.configs.properties.AppProperties;
import com.carx.slackbot.enums.AdminMessageType;
import com.carx.slackbot.models.EventMessageModel;

/**
 * @author Created by KarpuninVD on 14.12.2022
 */
public interface SlackBotService {
    void sendMessageToAllUsers(EventMessageModel message, AppProperties.Inform inform);

    void sendMessageToChannel(String channelId, EventMessageModel message, AppProperties.Inform inform);

    void publishHomePage(EventMessageModel message, AppProperties.Inform inform);

    void updateViewIfChanged();

    void sendMessageToAdmins(String message, AdminMessageType messageType);
}
