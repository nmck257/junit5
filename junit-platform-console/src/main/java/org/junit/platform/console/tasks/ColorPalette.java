package org.junit.platform.console.tasks;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.platform.console.tasks.Style.NONE;

public class ColorPalette {
    private final Map<Style, String> colorsToAnsiSequences;
    private final boolean disableAnsiColors;

    // http://ascii-table.com/ansi-escape-sequences.php
    private static Map<Style, String> defaultPalette() {
        Map<Style, String> colorsToAnsiSequences = new EnumMap<>(Style.class);
        colorsToAnsiSequences.put(Style.NONE, "0");
        colorsToAnsiSequences.put(Style.SUCCESSFUL, "32");
        colorsToAnsiSequences.put(Style.ABORTED, "33");
        colorsToAnsiSequences.put(Style.FAILED, "31");
        colorsToAnsiSequences.put(Style.SKIPPED, "35");
        colorsToAnsiSequences.put(Style.CONTAINER, "36");
        colorsToAnsiSequences.put(Style.TEST, "34");
        colorsToAnsiSequences.put(Style.DYNAMIC, "35");
        colorsToAnsiSequences.put(Style.REPORTED, "37");
        return colorsToAnsiSequences;
    }

    public ColorPalette(Map<Style, String> overrides) {
        this(defaultPalette(), false);

        if (overrides.containsKey(NONE)) {
            throw new IllegalArgumentException("Cannot override the standard style 'NONE'");
        }
        this.colorsToAnsiSequences.putAll(overrides);
    }

    public static ColorPalette NONE() {
        return new ColorPalette(new HashMap<>(), true);
    }

    public static ColorPalette DEFAULT() {
        return new ColorPalette(defaultPalette(), false);
    }

    private ColorPalette(Map<Style, String> colorsToAnsiSequences, boolean disableAnsiColors) {
        this.colorsToAnsiSequences = colorsToAnsiSequences;
        this.disableAnsiColors = disableAnsiColors;
    }

    public String paint(Style style, String text) {
        return disableAnsiColors || style == NONE
                ? text
                : getAnsiFormatter(style) + text + getAnsiFormatter(NONE);
    }

    private String getAnsiFormatter(Style style) {
        return String.format("\u001B[%sm", colorsToAnsiSequences.get(style));
    }
}
