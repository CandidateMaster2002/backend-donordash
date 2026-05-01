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

    public static final String QR_V2_PAYMENT = "QRv2 Payment";
    public static final String CAPTURED = "captured";
    public static final String DESCRIPTION = "description";
    public static final String CAPTURED_AT = "captured_at";
    public static final String ACQUIRER_DATA = "acquirer_data";
    public static final String AMOUNT = "amount";
    public static final String NOTES = "notes";
    public static final String ID = "id";

    public static final String BANK_TRANSFER = "Bank Transfer";
    public static final String RAZORPAY_LINK = "Razorpay Link";
    public static final int DUPLICATE_CHECK_DAYS = 30;

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
