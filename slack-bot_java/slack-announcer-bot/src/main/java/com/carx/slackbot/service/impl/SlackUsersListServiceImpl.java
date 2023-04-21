package com.carx.slackbot.service.impl;

import com.carx.slackbot.configs.properties.SlackBotProperties;
import com.carx.slackbot.service.SlackUsersListService;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author Created by KarpuninVD on 18.01.2023
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.debug", havingValue = "false")
public class SlackUsersListServiceImpl implements SlackUsersListService {
    private final SlackBotProperties botProps;
    private MethodsClient client;

    @PostConstruct
    public void init() {
        this.client = Slack.getInstance().methods();
    }

    @Override
    @Cacheable(value = "SLACK_USERS")
    public List<User> getAllUsers() throws SlackApiException, IOException {
        //fixme
        log.info("GETTING USERS LIST FROM SLACK");
        UsersListResponse usersListResponse = client.usersList(builder -> builder.token(botProps.getToken()));
        return usersListResponse.getMembers();
    }
}
