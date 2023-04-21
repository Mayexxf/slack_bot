package com.carx.slackbot.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Comparator;
import java.util.List;

/**
 * @author Created by KarpuninVD on 19.01.2023
 */
@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private List<Inform> inform;

    public List<Inform> getInform() {
        inform.sort(Comparator.comparingInt(Inform::getTime).reversed());
        return inform;
    }

    @Data
    public static class Inform {
        private int time;
        private String message;
    }
}
