package org.junit.platform.console.tasks;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.platform.console.tasks.Style.NONE;

public class ColorPalette {
    private final Map<Style, String> colorsToAnsiSequences;
    private final boolean disableAnsiColors;

    // https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_parameters
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

    private static Map<Style, String> singleColorPalette() {
        Map<Style, String> colorsToAnsiSequences = new EnumMap<>(Style.class);
        colorsToAnsiSequences.put(Style.NONE, "0");
        colorsToAnsiSequences.put(Style.SUCCESSFUL, "1");
        colorsToAnsiSequences.put(Style.ABORTED, "4");
        colorsToAnsiSequences.put(Style.FAILED, "7");
        colorsToAnsiSequences.put(Style.SKIPPED, "9");
        colorsToAnsiSequences.put(Style.CONTAINER, "1");
        colorsToAnsiSequences.put(Style.TEST, "0");
        colorsToAnsiSequences.put(Style.DYNAMIC, "0");
        colorsToAnsiSequences.put(Style.REPORTED, "2");
        return colorsToAnsiSequences;
    }

    public static ColorPalette SINGLE_COlOR() {
        return new ColorPalette(singleColorPalette(), false);
    }

    public static ColorPalette DEFAULT() {
        return new ColorPalette(defaultPalette(), false);
    }

    public static ColorPalette NONE() {
        return new ColorPalette(new HashMap<>(), true);
    }

    public ColorPalette(Map<Style, String> overrides) {
        this(defaultPalette(), false);

        if (overrides.containsKey(NONE)) {
            throw new IllegalArgumentException("Cannot override the standard style 'NONE'");
        }
        this.colorsToAnsiSequences.putAll(overrides);
    }

    public ColorPalette(Properties properties) {
        this(toOverrideMap(properties));
    }

    public ColorPalette(Reader reader) {
        this(getProperties(reader));
    }

    public ColorPalette(Path path) {
        this(openReader(path));
    }

    private ColorPalette(Map<Style, String> colorsToAnsiSequences, boolean disableAnsiColors) {
        this.colorsToAnsiSequences = colorsToAnsiSequences;
        this.disableAnsiColors = disableAnsiColors;
    }

    private static Map<Style, String> toOverrideMap(Properties properties) {
        Map<String, String> upperCaseProperties = properties.entrySet().stream().collect(
                Collectors.toMap(entry -> ((String) entry.getKey()).toUpperCase(), entry -> (String) entry.getValue()));

        return Arrays.stream(Style.values())
                .filter(style -> upperCaseProperties.containsKey(style.name()))
                .collect(Collectors.toMap(Function.identity(), style -> upperCaseProperties.get(style.name())));
    }

    private static Properties getProperties(Reader reader) {
        Properties properties = new Properties();
        try {
            properties.load(reader);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read color palette properties", e);
        }
        return properties;
    }

    private static Reader openReader(Path path) {
        try {
            return new FileReader(path.toFile());
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not open color palette properties file", e);
        }
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
