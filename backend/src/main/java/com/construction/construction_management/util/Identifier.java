package com.construction.construction_management.util;

import com.construction.construction_management.exception.BadRequestException;

import java.util.regex.Pattern;

/** A login id typed by the user: either an Indian mobile number or an email address. */
public record Identifier(String value, boolean email) {

    private static final Pattern PHONE = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public boolean isPhone() {
        return !email;
    }

    public static Identifier parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("Enter your mobile number or email");
        }
        String s = raw.trim();
        if (s.contains("@")) {
            String e = s.toLowerCase();
            if (!EMAIL.matcher(e).matches()) {
                throw new BadRequestException("Enter a valid email address");
            }
            return new Identifier(e, true);
        }
        return phone(s);
    }

    /** Accepts 9876543210, +91 98765 43210, 919876543210 and 09876543210. */
    public static Identifier phone(String raw) {
        String d = raw == null ? "" : raw.trim().replaceAll("[\\s-]", "");
        if (d.startsWith("+91")) {
            d = d.substring(3);
        } else if (d.length() == 12 && d.startsWith("91")) {
            d = d.substring(2);
        } else if (d.length() == 11 && d.startsWith("0")) {
            d = d.substring(1);
        }
        if (!PHONE.matcher(d).matches()) {
            throw new BadRequestException("Enter a valid 10-digit mobile number or email");
        }
        return new Identifier(d, false);
    }

    public static Identifier email(String raw) {
        Identifier id = parse(raw);
        if (!id.email) {
            throw new BadRequestException("Enter a valid email address");
        }
        return id;
    }

    /** 98******10 or a***@gmail.com - safe to show on screen. */
    public String masked() {
        if (email) {
            int at = value.indexOf('@');
            String local = value.substring(0, at);
            return local.charAt(0) + "***" + value.substring(at);
        }
        return value.substring(0, 2) + "******" + value.substring(8);
    }
}
