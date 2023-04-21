package com.carx.slackbot.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * @author Created by KarpuninVD on 09.01.2023
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventMessageModel implements Comparable<EventMessageModel> {

    private Integer id;
    private LocalDate date;
    private LocalTime beginTime;
    private String place;
    private LocalTime endTime;
    private List<String> speakersId;
    private String title;
    private String streamUrl;

    @Override
    public int compareTo(@NotNull EventMessageModel o) {
        if (this.date.isBefore(o.date)) {
            return 1;
        } else {
            return this.beginTime.compareTo(o.beginTime);
        }
    }
    public EventMessageModel(EventMessageModel copyFrom) {
        this.id = copyFrom.id;
        this.date = copyFrom.date;
        this.beginTime = copyFrom.beginTime;
        this.place = copyFrom.place;
        this.endTime = copyFrom.endTime;
        this.speakersId = copyFrom.speakersId;
        this.title = copyFrom.title;
        this.streamUrl = copyFrom.streamUrl;
    }
}
