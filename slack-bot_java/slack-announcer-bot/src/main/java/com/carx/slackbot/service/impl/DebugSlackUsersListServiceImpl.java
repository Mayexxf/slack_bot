package com.carx.slackbot.service.impl;

import com.carx.slackbot.configs.properties.SlackBotProperties;
import com.carx.slackbot.service.SlackUsersListService;
import com.slack.api.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Created by KarpuninVD on 18.01.2023
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.debug", havingValue = "true")
public class DebugSlackUsersListServiceImpl implements SlackUsersListService {
    private final SlackBotProperties botProps;

    @Override
    public List<User> getAllUsers() {
        log.info("GETTING ADMIN USERS IDS");
        return botProps.getAdminIds().stream()
                .map(id -> {
                    final User user = new User();
                    user.setId(id);
                    return user;
                }).toList();
    }
}
