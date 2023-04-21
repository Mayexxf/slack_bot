package com.carx.slackbot.configs.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Created by KarpuninVD on 18.01.2023
 */
@Data
@ConfigurationProperties(prefix = "resource.file-path")
public class ResourcePathProperties {
    private String yandex;
    private String localFile;
}
