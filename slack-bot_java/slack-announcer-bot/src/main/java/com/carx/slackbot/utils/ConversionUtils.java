package com.carx.slackbot.utils;

import com.carx.slackbot.models.EventMessageModel;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.ConversionService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by KarpuninVD on 18.01.2023
 */
public class ConversionUtils {
    private ConversionUtils() {
    }

    @NotNull
    public static List<EventMessageModel> getEventMessageModelsFromWorkbook(Workbook workbook,
            ConversionService conversionService) {
        ArrayList<EventMessageModel> result = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            final Row row = sheet.getRow(i);
            if (row != null && row.getCell(0) != null && row.getCell(0).getCellType() != CellType.BLANK) {
                result.add(conversionService.convert(row, EventMessageModel.class));
            }
        }
        return result;
    }

}
