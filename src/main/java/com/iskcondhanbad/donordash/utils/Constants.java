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
}
