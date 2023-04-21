package com.carx.slackbot.service.impl;

import com.carx.slackbot.configs.properties.ResourcePathProperties;
import com.carx.slackbot.enums.AdminMessageType;
import com.carx.slackbot.models.EventMessageModel;
import com.carx.slackbot.service.ScheduleResourceService;
import com.carx.slackbot.service.SlackBotService;
import com.carx.slackbot.utils.ConversionUtils;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.json.Resource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Created by KarpuninVD on 10.01.2023
 */
@Component("yandex-disk")
@RequiredArgsConstructor
@Slf4j
public class YandexDiskScheduleResourceServiceImpl implements ScheduleResourceService {

    @Value("${yandex.api.token}")
    private String token;
    private final ConversionService conversionService;
    private final SlackBotService botService;
    private final ResourcePathProperties filePathProps;
    private RestClient restClient;
    private Date modified = null;

    @PostConstruct
    public void init() {
        restClient = new RestClient(new Credentials(null, token));
    }

    @Override
    public boolean updateResourceFileIfNeeded() {
        if (modified == null) {
            return true;
        }
        final boolean result = !modified.equals(getResources().getModified());
        log.info("Is need to update file - {}", result);
        return result;
    }

    @Override
    public List<EventMessageModel> getSlackBlockMessagesFromFile() {
        File file = new File("schedule.xlsx");
        if (file.exists()) {
            file.delete();
        }
        try {
            restClient.downloadFile(filePathProps.getYandex(), file, new ProgressListener() {
                @Override
                public void updateProgress(long l, long l1) {
                    if (l >= l1) {
                        log.info("File is successfully downloaded from Yandex.Disk");
                        botService.sendMessageToAdmins("File has been downloaded from Yandex.Disk", AdminMessageType.INFO);
                        modified = getResources().getModified();
                    }
                }

                @Override
                public boolean hasCancelled() {
                    return false;
                }
            });

            try (XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file))) {
                return ConversionUtils.getEventMessageModelsFromWorkbook(workbook, conversionService);
            }
        } catch (IOException | ServerException e) {
            botService.sendMessageToAdmins("Cannot download message from Yandex.Disk. ERROR: " + e.getMessage(),
                    AdminMessageType.ERROR);
            log.error("Cannot download message from Yandex.Disk", e);
            throw new RuntimeException(e);
        }
    }

    private Resource getResources() {
        try {
            return restClient.getResources(new ResourcesArgs.Builder()
                    .setPath(filePathProps.getYandex())
                    .build());
        } catch (IOException | ServerIOException e) {
            botService.sendMessageToAdmins("Cannot get resource from Yandex.Disk. ERROR: " + e.getMessage(),
                    AdminMessageType.ERROR);
            log.error("Cannot download message from Yandex.Disk", e);
            return new Resource();
        }
    }
}
