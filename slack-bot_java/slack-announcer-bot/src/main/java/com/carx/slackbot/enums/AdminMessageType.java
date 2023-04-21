package com.carx.slackbot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Created by KarpuninVD on 18.01.2023
 */
@AllArgsConstructor
@Getter
public enum AdminMessageType {
    INFO("information_source"),
    WARN("warning"),
    ERROR("x");

    private String emoji;
}
