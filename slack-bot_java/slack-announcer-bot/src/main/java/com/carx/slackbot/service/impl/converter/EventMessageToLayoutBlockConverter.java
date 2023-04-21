package com.carx.slackbot.service.impl.converter;

import com.carx.slackbot.models.EventMessageModel;
import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by KarpuninVD on 17.01.2023
 * Конвертер модели сообщения в формат слак сообщения
 */
@Component
public class EventMessageToLayoutBlockConverter implements Converter<EventMessageModel, List<LayoutBlock>> {
    @Override
    public List<LayoutBlock> convert(EventMessageModel item) {
        StringBuilder messageBuilder = new StringBuilder(String.format(">Время:  *%s - %s*\n>Место: *%s*\n",
                item.getBeginTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                item.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")), item.getPlace()));

        if (item.getSpeakersId() != null && !item.getSpeakersId().isEmpty()) {
            if (item.getSpeakersId().size() > 1) {
                messageBuilder.append(">Спикеры: ");
                for (String speaker : item.getSpeakersId()) {
                    messageBuilder.append(String.format("<@%s> ", speaker));
                }
            } else {
                messageBuilder.append(String.format(">Спикер: <@%s>", item.getSpeakersId().get(0)));
            }
        }

        List<LayoutBlock> message = new ArrayList<>(List.of(
                new HeaderBlock("title_" + item.getId(), new PlainTextObject(item.getTitle(), false)),
                new ContextBlock(List.of(new MarkdownTextObject(messageBuilder.toString(), false)), "msg_" + item.getId())));

        if (item.getSpeakersId() != null && !item.getSpeakersId().isEmpty()) {
            message.addAll(List.of(
                    new DividerBlock(),
                    new ActionsBlock(
                            List.of(new ButtonElement(new PlainTextObject("Стрим :movie_camera:", true), null,
                                    item.getStreamUrl(), null,
                                    "primary", null, null)), "buttons_" + item.getId())
            ));
        }

        return message;
    }
}
