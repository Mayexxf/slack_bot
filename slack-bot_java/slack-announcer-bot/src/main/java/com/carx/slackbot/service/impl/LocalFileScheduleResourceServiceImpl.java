package com.carx.slackbot.service.impl;

import com.carx.slackbot.configs.properties.ResourcePathProperties;
import com.carx.slackbot.models.EventMessageModel;
import com.carx.slackbot.service.ScheduleResourceService;
import com.carx.slackbot.utils.ConversionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * @author Created by KarpuninVD on 18.01.2023
 */
@Component("local-file")
@RequiredArgsConstructor
@Slf4j
public class LocalFileScheduleResourceServiceImpl implements ScheduleResourceService {

    private final ConversionService conversionService;
    private final ResourcePathProperties pathProperties;

    private long lastModified;

    @Override
    public List<EventMessageModel> getSlackBlockMessagesFromFile() {
        try {
            final File file = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath() +
                    pathProperties.getLocalFile());
            lastModified = file.lastModified();
            try (XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file))) {
                return ConversionUtils.getEventMessageModelsFromWorkbook(workbook, conversionService);
            } catch (IOException e) {
                log.error("Cannot get data from local file", e);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Cannot get data from local file", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean updateResourceFileIfNeeded() {
        try {
            final File file = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath() +
                    pathProperties.getLocalFile());
            if (lastModified != file.lastModified()) {
                log.info("Is need to update file - {}", true);
                return true;
            }
        } catch (URISyntaxException e) {
            log.error("Cannot get data from local file", e);
        }
        log.info("Is need to update file - {}", false);
        return false;
    }
}
