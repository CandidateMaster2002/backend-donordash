package com.iskcondhanbad.donordash.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class SummaryRequest {

    private String parameter;
    private Integer collectedById;
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dateTo;

}