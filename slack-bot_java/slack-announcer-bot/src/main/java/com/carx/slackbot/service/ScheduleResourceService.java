package com.carx.slackbot.service;

import com.carx.slackbot.models.EventMessageModel;

import java.util.List;

/**
 * @author Created by KarpuninVD on 10.01.2023
 */
public interface ScheduleResourceService {
    List<EventMessageModel> getSlackBlockMessagesFromFile();

    boolean updateResourceFileIfNeeded();
}
