package com.carx.slackbot;

import com.carx.slackbot.configs.properties.AppProperties;
import com.carx.slackbot.configs.properties.ResourcePathProperties;
import com.carx.slackbot.configs.properties.SlackBotProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@ServletComponentScan
@EnableConfigurationProperties({SlackBotProperties.class, ResourcePathProperties.class, AppProperties.class})
public class SlackbotApplication {
    public static void main(String[] args) {
        SpringApplication.run(SlackbotApplication.class, args);
    }
}
