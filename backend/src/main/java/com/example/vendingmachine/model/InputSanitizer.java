package com.example.vendingmachine.model;


public class InputSanitizer {

    public static String sanitize(String input) {
        if (input == null) return null;
        return input.trim()
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    public static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 不能為空");
        }
    }

    public static void validateMaxLength(String value, String fieldName, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " 長度不能超過 " + maxLength + " 字元");
        }
    }

    public static void validatePositive(Number value, String fieldName) {
        if (value != null && value.doubleValue() < 0) {
            throw new IllegalArgumentException(fieldName + " 不能為負數");
        }
    }
}