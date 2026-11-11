package com.iskcondhanbad.donordash.utils;

public class NumberToWordsUtil {

    private static final String[] UNITS = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};
    private static final String[] TEENS = {"Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
    private static final String[] TENS = {"", "Ten", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

    private static String convertChunk(long num) {
        if (num == 0) return "";
        StringBuilder chunkWords = new StringBuilder();
        if (num >= 100) {
            chunkWords.append(UNITS[(int) (num / 100)]).append(" Hundred ");
            num %= 100;
        }
        if (num >= 20) {
            chunkWords.append(TENS[(int) (num / 10)]).append(" ");
            num %= 10;
        } else if (num >= 10) {
            chunkWords.append(TEENS[(int) (num - 10)]).append(" ");
            num = 0;
        }
        if (num > 0) {
            chunkWords.append(UNITS[(int) num]).append(" ");
        }
        return chunkWords.toString().trim();
    }

    public static String convertToWords(long number) {
        if (number == 0) return "Zero Rupees Only";

        StringBuilder words = new StringBuilder();
        if (number >= 10000000) {
            long crore = number / 10000000;
            words.append(convertChunk(crore)).append(" Crore ");
            number %= 10000000;
        }
        if (number >= 100000) {
            long lakh = number / 100000;
            words.append(convertChunk(lakh)).append(" Lakh ");
            number %= 100000;
        }
        if (number >= 1000) {
            long thousand = number / 1000;
            words.append(convertChunk(thousand)).append(" Thousand ");
            number %= 1000;
        }
        if (number > 0) {
            words.append(convertChunk(number)).append(" ");
        }

        return words.toString().trim() + " Rupees Only";
    }
}

