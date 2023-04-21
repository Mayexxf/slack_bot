package com.carx.slackbot.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author Created by KarpuninVD on 14.12.2022
 */
@Data
@ConfigurationProperties(prefix = "slack.bot")
public class SlackBotProperties {
    private String token;
    private List<String> adminIds;
    private String channelStart;
    private String admin;
}
