package com.carx.slackbot.service.impl.converter;

import com.carx.slackbot.models.EventMessageModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @author Created by KarpuninVD on 17.01.2023
 * Конвертер строки в модель сообщения об ивенте
 */
@Component
@Slf4j
public class RowToEventMessageModelConverter implements Converter<Row, EventMessageModel> {

    @NotNull
    private static RuntimeException getRuntimeException(Row row, int cell) {
        return new RuntimeException("Column " + (cell + 1) + " is null, row " + row.getRowNum());
    }

    @Override
    public EventMessageModel convert(Row row) {
        final Integer id = Optional.ofNullable(row.getCell(0))
                .map($ -> (int) $.getNumericCellValue())
                .orElseThrow(() -> getRuntimeException(row, 1));

        final LocalDate date = Optional.ofNullable(row.getCell(1))
                .map(Cell::getLocalDateTimeCellValue)
                .map(LocalDateTime::toLocalDate)
                .orElseThrow(() -> getRuntimeException(row, 1));

        final LocalTime beginTime = Optional.ofNullable(row.getCell(2))
                .map(Cell::getLocalDateTimeCellValue)
                .map(LocalDateTime::toLocalTime)
                .orElseThrow(() -> getRuntimeException(row, 2));

        final LocalTime endTime = Optional.ofNullable(row.getCell(3))
                .map(Cell::getLocalDateTimeCellValue)
                .map(LocalDateTime::toLocalTime)
                .orElseThrow(() -> getRuntimeException(row, 3));

        final List<String> speakerId = Optional.ofNullable(row.getCell(4))
                .map(Cell::getStringCellValue)
                .map($ -> {
                    if ($.isEmpty()) {
                        return new ArrayList<String>();
                    }
                    return Arrays.stream($.split(","))
                            .map(String::trim)
                            .toList();
                })
                .orElse(Collections.emptyList());

        final String title = Optional.ofNullable(row.getCell(5))
                .map(Cell::getStringCellValue)
                .orElseThrow(() -> getRuntimeException(row, 5));

        final String streamUrl = Optional.ofNullable(row.getCell(6))
                .map(Cell::getStringCellValue)
                .orElse(null);

        final String place = Optional.ofNullable(row.getCell(7))
                .map(Cell::getStringCellValue)
                .orElseThrow(() -> getRuntimeException(row, 7));

        return new EventMessageModel(
                id,
                date,
                beginTime,
                place,
                endTime,
                speakerId, title, streamUrl
        );
    }
}
