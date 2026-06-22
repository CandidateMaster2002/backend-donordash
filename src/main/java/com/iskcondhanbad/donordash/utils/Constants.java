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
    public static final String CASH = "Cash";
    public static final String SHYAM_CASH = "Shyam Cash";

    // Donation statuses
    public static final String VERIFIED = "Verified";
    public static final String CANCELLED = "Cancelled";
    public static final String PENDING = "Pending";
    public static final String UNAPPROVED = "Unapproved";

    public static final String NO_RECEIPT = "no_receipt";

    // Donor categories
    public static final String VERY_BIG_DONOR = "Very Big Donor";
    public static final String MEDIUM_DONOR = "Medium Donor";
    public static final String SUPPORTER_DONOR = "Supporter Donor";
    public static final String ENTRY_LEVEL_DONOR = "Entry-Level Donor";
    public static final String GENERAL_DONOR = "General Donor";
    public static final String ONE_TIMER = "One Timer";
    public static final String NITYA_SEVAK = "Nitya Sevak";

    public static final int DUPLICATE_CHECK_DAYS = 30;

    public static final double CASH_DONATION_PAN_THRESHOLD = 49_999.0;
    public static final double CUMULATIVE_DONATION_PAN_THRESHOLD = 100_000.0;

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
