package com.mojang.realmsclient.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public enum ChatFormatting {
    BLACK('0'), DARK_BLUE('1'), DARK_GREEN('2'), DARK_AQUA('3'), DARK_RED('4'), DARK_PURPLE('5'), GOLD('6'), GRAY('7'), DARK_GRAY('8'),
    BLUE('9'), GREEN('a'), AQUA('b'), RED('c'), LIGHT_PURPLE('d'), YELLOW('e'), WHITE('f'),
    OBFUSCATED('k', true), BOLD('l', true), STRIKETHROUGH('m', true), UNDERLINE('n', true), ITALIC('o', true), RESET('r');

    public static final char PREFIX_CODE = '§';
    private static final Map<Character, ChatFormatting> FORMATTING_BY_CHAR = new HashMap<>();
    private static final Map<String, ChatFormatting> FORMATTING_BY_NAME = new HashMap<>();
    private static final Pattern STRIP_FORMATTING_PATTERN = Pattern.compile("(?i)" + PREFIX_CODE + "[0-9A-FK-OR]");
    private final char code;
    private final boolean isFormat;
    private final String toString;

    ChatFormatting(char code) {
        this(code, false);
    }

    ChatFormatting(char code, boolean isFormat) {
        this.code = code;
        this.isFormat = isFormat;
        this.toString = "" + PREFIX_CODE + code;
    }

    static {
        for (ChatFormatting format : values()) {
            FORMATTING_BY_CHAR.put(format.code, format);
            FORMATTING_BY_NAME.put(format.name().toLowerCase(), format);
        }
    }

    public char getChar() {
        return code;
    }

    public boolean isFormat() {
        return isFormat;
    }

    public boolean isColor() {
        return !isFormat && this != RESET;
    }

    public String getName() {
        return name().toLowerCase();
    }

    @Override
    public String toString() {
        return toString;
    }

    public static String stripFormatting(String input) {
        return input == null ? null : STRIP_FORMATTING_PATTERN.matcher(input).replaceAll("");
    }

    public static ChatFormatting getByChar(char code) {
        return FORMATTING_BY_CHAR.get(code);
    }

    public static ChatFormatting getByName(String name) {
        return name == null ? null : FORMATTING_BY_NAME.get(name.toLowerCase());
    }
}
