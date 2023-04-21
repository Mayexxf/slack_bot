package com.carx.slackbot.service;

import com.slack.api.methods.SlackApiException;
import com.slack.api.model.User;

import java.io.IOException;
import java.util.List;

/**
 * @author Created by KarpuninVD on 18.01.2023
 */
public interface SlackUsersListService {
    List<User> getAllUsers() throws SlackApiException, IOException;
}
