package com.construction.construction_management.util;

import java.math.BigDecimal;
import java.util.List;

/**
 * Small CSV writer used by pilot exports. Every text cell is quoted so commas,
 * quotes and newlines are safe. Text that starts with a spreadsheet formula
 * character is prefixed with an apostrophe to prevent CSV injection.
 */
public final class Csv {

    private Csv() {
    }

    public static String document(List<String> rows) {
        return "\uFEFF" + String.join("\r\n", rows) + "\r\n";
    }

    public static String row(Object... values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(cell(values[i]));
        }
        return line.toString();
    }

    private static String cell(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (value instanceof Number number) {
            return number.toString();
        }
        if (value instanceof Boolean bool) {
            return bool.toString();
        }
        String text = value.toString();
        if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
