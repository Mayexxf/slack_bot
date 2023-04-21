package com.carx.slackbot.utils;

import java.time.*;

/**
 * @author Created by KarpuninVD on 27.01.2023
 */
public class DateTimeUtils {

    private static final ZoneId zone = ZoneId.of("+3");

    private DateTimeUtils() {
    }

    public static ZoneOffset getDefaultOffset() {
        Instant instant = Instant.now(); //can be LocalDateTime
        ZoneId systemZone = ZoneId.systemDefault(); // my timezone
        return systemZone.getRules().getOffset(instant);
    }

    public static LocalDate nowDate() {
        return LocalDateTime.now().atZone(getDefaultOffset()).withZoneSameInstant(zone).toLocalDate();
    }

    public static LocalTime nowTime() {
        return LocalDateTime.now().atZone(getDefaultOffset()).withZoneSameInstant(zone).toLocalTime();
    }

}
