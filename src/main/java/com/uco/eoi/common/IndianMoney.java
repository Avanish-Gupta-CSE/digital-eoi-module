package com.uco.eoi.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formats an amount the way a bank officer reads it: Indian digit grouping
 * (last three digits, then groups of two) with a rupee sign.
 *
 * 250000000 -> "₹ 25,00,00,000.00"
 *
 * One formatter, used by every screen. If the bank asks for a different style,
 * this is the only file to change.
 */
public final class IndianMoney {

    private IndianMoney() {
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        BigDecimal rounded = amount.setScale(2, RoundingMode.HALF_UP);
        boolean negative = rounded.signum() < 0;
        String plain = rounded.abs().toPlainString();
        int dot = plain.indexOf('.');
        String whole = plain.substring(0, dot);
        String fraction = plain.substring(dot + 1);

        String sign = negative ? "-" : "";
        return sign + "\u20B9 " + groupIndian(whole) + "." + fraction;
    }

    private static String groupIndian(String digits) {
        if (digits.length() <= 3) {
            return digits;
        }
        String lastThree = digits.substring(digits.length() - 3);
        String rest = digits.substring(0, digits.length() - 3);

        StringBuilder grouped = new StringBuilder();
        int index = 0;
        if (rest.length() % 2 == 1) {
            grouped.append(rest.charAt(0));
            index = 1;
            if (rest.length() > 1) {
                grouped.append(',');
            }
        }
        for (; index < rest.length(); index += 2) {
            grouped.append(rest, index, index + 2);
            if (index + 2 < rest.length()) {
                grouped.append(',');
            }
        }
        return grouped + "," + lastThree;
    }
}
