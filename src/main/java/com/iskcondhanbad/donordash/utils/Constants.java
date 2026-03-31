package com.iskcondhanbad.donordash.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Constants {
    private Constants() {
        // prevent instantiation
    }

    public static final Date DEFAULT_FROM_DATE =
            Date.from(
                    LocalDate.of(2025, 1, 1)
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
            );
    public static final String PURPOSE = "purpose";
    public static final String ZONE = "zone";
    public static final String PAYMENT_MODE = "payment_mode";
    public static final String CULTIVATOR = "cultivator";

    public static Date getCurrentFinancialYearStart() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        
        LocalDate fyStart = LocalDate.of(
                today.isBefore(LocalDate.of(year, 4, 2)) ? year - 1 : year,
                4,
                1
        );
        return Date.from(fyStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date getCurrentFinancialYearEnd() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        
        LocalDate fyEnd = LocalDate.of(
                today.isBefore(LocalDate.of(year, 4, 2)) ? year : year + 1,
                3,
                31
        );
        return Date.from(fyEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
    }
}
