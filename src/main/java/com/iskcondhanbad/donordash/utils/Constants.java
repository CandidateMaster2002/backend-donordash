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
}
